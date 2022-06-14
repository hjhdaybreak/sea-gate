package com.sea.cache;

import com.google.common.collect.Lists;
import com.sea.pojo.dto.ServiceInstance;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceCache {

    private static final Map<String, List<ServiceInstance>> SERVICE_MAP = new ConcurrentHashMap<>();


    public static List<ServiceInstance> getAllInstances(String serviceName) {
        return SERVICE_MAP.get(serviceName);
    }


    public static void add(String serviceName, List<ServiceInstance> list) {
        SERVICE_MAP.put(serviceName, list);
    }

    public static void removeExpired(List<String> onlineServices) {
        List<String> expiredKeys = Lists.newLinkedList();
        SERVICE_MAP.keySet().forEach(k -> {
            if (!onlineServices.contains(k)) {
                expiredKeys.add(k);
            }
        });
        expiredKeys.forEach(expiredKey -> SERVICE_MAP.remove(expiredKey));
    }

}
