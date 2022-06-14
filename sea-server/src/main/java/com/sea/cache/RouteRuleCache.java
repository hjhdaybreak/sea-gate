package com.sea.cache;


import com.sea.exception.SeaException;
import com.sea.pojo.dto.AppRuleDTO;


import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class RouteRuleCache {

    private static final Map<String, List<AppRuleDTO>> ROUTE_RULE_MAP = new ConcurrentHashMap<>();

    public static void add(Map<String, List<AppRuleDTO>> map) {
        ROUTE_RULE_MAP.putAll(map);
    }

    public static void remove(Map<String, List<AppRuleDTO>> map) {
        // todo
    }


    /**
     * get rules by appName
     *
     * @param appName
     * @return
     */
    public static List<AppRuleDTO> getRules(String appName) {
        List<AppRuleDTO> value = ROUTE_RULE_MAP.get(appName);
        if (value == null) throw new SeaException("please config route rule in ship-admin first!");
        return value;
    }

}
