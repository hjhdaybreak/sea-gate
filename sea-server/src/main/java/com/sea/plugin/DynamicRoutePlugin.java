package com.sea.plugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sea.cache.LoadBalanceFactory;
import com.sea.cache.RouteRuleCache;
import com.sea.cache.ServiceCache;
import com.sea.chain.PluginChain;
import com.sea.chain.SeaResponseUtil;
import com.sea.config.ServerConfigProperties;
import com.sea.constant.MatchObjectEnum;
import com.sea.constant.SeaExceptionEnum;
import com.sea.constant.SeaPluginEnum;
import com.sea.exception.SeaException;
import com.sea.pojo.dto.AppRuleDTO;
import com.sea.pojo.dto.ServiceInstance;
import com.sea.spi.LoadBalance;
import com.sea.utils.StringTools;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeoutException;


public class DynamicRoutePlugin extends AbstractSeaPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicRoutePlugin.class);

    private Gson gson = new GsonBuilder().create();

    private static WebClient webClient;

    static {
        HttpClient httpClient = HttpClient.create()
                .tcpConfiguration(client ->
                        client.doOnConnected(conn ->
                                        conn.addHandlerLast(new ReadTimeoutHandler(3))
                                                .addHandlerLast(new WriteTimeoutHandler(3)))
                                .option(ChannelOption.TCP_NODELAY, true)
                );
        webClient = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }


    public DynamicRoutePlugin(ServerConfigProperties properties) {
        super(properties);
    }

    @Override
    public Integer order() {
        return SeaPluginEnum.DYNAMIC_ROUTE.getOrder();
    }

    @Override
    public String name() {
        return SeaPluginEnum.DYNAMIC_ROUTE.getName();
    }

    /**
     * @param exchange    当前请求和响应的上下文
     * @param pluginChain
     * @return
     */

    @Override
    public Mono<Void> execute(ServerWebExchange exchange, PluginChain pluginChain) {
        String appName = pluginChain.getAppName();
        ServiceInstance serviceInstance = chooseInstance(appName, exchange.getRequest());
        LOGGER.info("selected instance is [{}]", gson.toJson(serviceInstance));
        // request service
        String url = buildUrl(exchange, serviceInstance);
        return forward(exchange, url);
    }

    // 转发,带有超时转发
    private Mono<Void> forward(ServerWebExchange exchange, String url) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        HttpMethod method = request.getMethod();

        WebClient.RequestBodySpec requestBodySpec = webClient.method(method).uri(url).headers((headers -> {
            headers.addAll(request.getHeaders());
        }));

        WebClient.RequestHeadersSpec<?> reqHeadersSpec;

        if (requireHttpBody(method)) {
            reqHeadersSpec = requestBodySpec.body(BodyInserters.fromDataBuffers(request.getBody()));
        } else {
            reqHeadersSpec = requestBodySpec;
        }

        return reqHeadersSpec.exchange().timeout(Duration.ofMillis(properties.getTimeOutMillis()))
                .onErrorResume(ex -> {
                    return Mono.defer(() -> {
                        String errorResultJson = "";
                        if (ex instanceof TimeoutException) {
                            errorResultJson = "{\"code\":5001,\"message\":\"network timeout\"}";
                        } else {
                            errorResultJson = "{\"code\":5000,\"message\":\"system error\"}";
                        }
                        return SeaResponseUtil.doResponse(exchange, errorResultJson);
                    }).then(Mono.empty());
                }).flatMap(backendResponse -> {
                    response.setStatusCode(backendResponse.statusCode());
                    response.getHeaders().putAll(backendResponse.headers().asHttpHeaders());
                    return response.writeWith(backendResponse.bodyToFlux(DataBuffer.class));
                });
    }

    private boolean requireHttpBody(HttpMethod method) {
        //PATCH 对资源进行部分修改
        return method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT) || method.equals(HttpMethod.PATCH);
    }

    private String buildUrl(ServerWebExchange exchange, ServiceInstance serviceInstance) {
        ServerHttpRequest request = exchange.getRequest();
        String query = request.getURI().getQuery();
        String path = request.getPath().value().replaceFirst("/" + serviceInstance.getAppName(), "");
        String url = "http://" + serviceInstance.getIp() + ":" + serviceInstance.getPort() + path;
        if (!StringUtils.isEmpty(query)) {
            url = url + "?" + query;
        }
        return url;
    }

    private ServiceInstance chooseInstance(String appName, ServerHttpRequest request) {
        List<ServiceInstance> serviceInstances = ServiceCache.getAllInstances(appName);

        if (CollectionUtils.isEmpty(serviceInstances)) {
            LOGGER.error("service instance of {} not find", appName);
            throw new SeaException(SeaExceptionEnum.SERVICE_NOT_FIND);
        }

        String version = matchAppVersion(appName, request);
        if (StringUtils.isEmpty(version)) {
            throw new SeaException("match app version error");
        }

        // filter serviceInstances by version
        List<ServiceInstance> instances = new ArrayList<>();
        for (ServiceInstance serviceInstance : serviceInstances) {
            if (serviceInstance.getVersion().equals(version)) {
                instances.add(serviceInstance);
            }
        }

        //Select an instance based on the load balancing algorithm
        LoadBalance loadBalance = LoadBalanceFactory.getInstance(properties.getLoadBalance(), appName, version);
        return loadBalance.chooseOne(instances);
    }

    private String matchAppVersion(String appName, ServerHttpRequest request) {
        List<AppRuleDTO> rules = RouteRuleCache.getRules(appName);
        rules.sort(Comparator.comparing(AppRuleDTO::getPriority).reversed());
        for (AppRuleDTO rule : rules) {
            if (match(rule, request)) {
                return rule.getVersion();
            }
        }
        return null;
    }

    private boolean match(AppRuleDTO rule, ServerHttpRequest request) {
        String matchObject = rule.getMatchObject();
        String matchKey = rule.getMatchKey();
        String matchRule = rule.getMatchRule();
        Byte matchMethod = rule.getMatchMethod();

        // 如果默认的优先级最高直接返回
        if (MatchObjectEnum.DEFAULT.getCode().equals(matchObject)) {
            return true;
        } else if (MatchObjectEnum.QUERY.getCode().equals(matchObject)) {
            String param = request.getQueryParams().getFirst(matchKey);
            if (!StringUtils.isEmpty(param)) {
                return StringTools.match(param, matchMethod, matchRule);
            }
        } else if (MatchObjectEnum.HEADER.getCode().equals(matchObject)) {
            HttpHeaders headers = request.getHeaders();
            String headerValue = headers.getFirst(matchKey);
            if (!StringUtils.isEmpty(headerValue)) {
                return StringTools.match(headerValue, matchMethod, matchRule);
            }
        }
        return false;
    }
}
