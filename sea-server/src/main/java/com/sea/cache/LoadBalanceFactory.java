package com.sea.cache;

import com.sea.annotation.LoadBalanceAno;
import com.sea.exception.SeaException;
import com.sea.spi.LoadBalance;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public class LoadBalanceFactory {

    private static final Map<String, LoadBalance> LOAD_BALANCE_MAP = new ConcurrentHashMap<>();

    private LoadBalanceFactory() {

    }

    public static LoadBalance getInstance(final String name, String appName, String version) {
        String key = appName + ":" + version;
        return LOAD_BALANCE_MAP.computeIfAbsent(key, (k) -> getLoadBalance(name));
    }

    private static LoadBalance getLoadBalance(String name) {
        ServiceLoader<LoadBalance> loader = ServiceLoader.load(LoadBalance.class);
        for (LoadBalance loadBalance : loader) {
            LoadBalanceAno ano = loadBalance.getClass().getAnnotation(LoadBalanceAno.class);
            Assert.notNull(ano, "load balance name can not be empty!");
            if (name.equals(ano.value())) {
                return loadBalance;
            }
        }
        throw new SeaException("invalid load balance config");
    }
}
