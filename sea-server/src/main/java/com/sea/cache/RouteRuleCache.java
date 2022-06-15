package com.sea.cache;


import com.sea.exception.SeaException;
import com.sea.pojo.dto.AppRuleDTO;
import org.springframework.util.CollectionUtils;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RouteRuleCache {

    private static final Map<String, List<AppRuleDTO>> ROUTE_RULE_MAP = new ConcurrentHashMap<>();

    public static void add(Map<String, List<AppRuleDTO>> map) {
        ROUTE_RULE_MAP.putAll(map);
    }

    public static void remove(Map<String, List<AppRuleDTO>> map) {
        for (Map.Entry<String, List<AppRuleDTO>> entry : map.entrySet()) {
            String appName = entry.getKey();

            List<Integer> ruleIds = new ArrayList<>();
            for (AppRuleDTO appRuleDTO : entry.getValue()) {
                Integer id = appRuleDTO.getId();
                ruleIds.add(id);
            }
            List<AppRuleDTO> ruleDTOS = ROUTE_RULE_MAP.getOrDefault(appName, new ArrayList<>());

            List<AppRuleDTO> leftRules = new ArrayList<>();
            for (AppRuleDTO r : ruleDTOS) {
                if (!ruleIds.contains(r.getId())) {
                    leftRules.add(r);
                }
            }

            if (CollectionUtils.isEmpty(leftRules)) {
                // remove all
                ROUTE_RULE_MAP.remove(appName);
            } else {
                // remove some of them
                ROUTE_RULE_MAP.put(appName, leftRules);
            }
        }
    }


    /**
     * get rules by appName
     *
     * @param appName
     * @return
     */
    public static List<AppRuleDTO> getRules(String appName) {
        List<AppRuleDTO> value = ROUTE_RULE_MAP.get(appName);
        if (value == null) throw new SeaException("please config route rule in sea-admin first!");
        return value;
    }

}
