package com.sea.sync;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sea.constant.OperationTypeEnum;
import com.sea.exception.SeaException;
import com.sea.pojo.dto.AppRuleDTO;
import com.sea.pojo.dto.RouteRuleOperationDTO;
import com.sea.service.RuleService;
import com.sea.utils.SeaThreadFactory;
import com.sea.utils.StringTools;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.java_websocket.client.WebSocketClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


@Component
public class WebsocketSyncCacheHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(WebsocketSyncCacheHandler.class);

    private WebSocketClient client;

    private Gson gson = new GsonBuilder().create();

    public WebsocketSyncCacheHandler(@Value("${sea.server-web-socket-url}") String serverWebSocketUrl,
                                     RuleService ruleService) {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1,
                new SeaThreadFactory("websocket-connect", true).create());
        try {
            client = new WebSocketClient(new URI(serverWebSocketUrl)) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    LOGGER.info("client is open");
                    List<AppRuleDTO> list = ruleService.getEnabledRule();
                    String msg = gson.toJson(new RouteRuleOperationDTO(OperationTypeEnum.INSERT, list));
                    send(msg);
                }

                @Override
                public void onMessage(String s) {

                }

                @Override
                public void onClose(int i, String s, boolean b) {

                }

                @Override
                public void onError(Exception e) {
                    LOGGER.error("websocket client error", e);
                }
            };

            client.connectBlocking();
            //使用调度线程池进行断线重连,30 秒进行一次
            executor.scheduleAtFixedRate(() -> {
                if (client != null && client.isClosed()) {
                    try {
                        client.reconnectBlocking();
                    } catch (InterruptedException e) {
                        LOGGER.error("reconnect server fail", e);
                    }
                }
            }, 10, 30, TimeUnit.SECONDS);

        } catch (Exception e) {
            LOGGER.error("websocket sync cache exception", e);
            throw new SeaException(e.getMessage());
        }
    }


    public void send(String content) {
        while (!client.getReadyState().equals(ReadyState.OPEN)) {
            LOGGER.debug("connecting ...please wait");
        }
        client.send(content);
    }
}