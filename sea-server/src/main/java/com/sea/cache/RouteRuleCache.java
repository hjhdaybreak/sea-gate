package com.sea.cache;


import com.sea.exception.SeaException;
import com.sea.pojo.dto.AppRuleDTO;
import org.springframework.util.CollectionUtils;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RouteRuleCache {

    private static final Map<String, CopyOnWriteArrayList<AppRuleDTO>> ROUTE_RULE_MAP = new ConcurrentHashMap<>();

    public static void add(Map<String, List<AppRuleDTO>> map) {
        map.forEach((key, value) -> {
            ROUTE_RULE_MAP.put(key, new CopyOnWriteArrayList(value));
        });
    }

    public static void remove(Map<String, List<AppRuleDTO>> map) {
        for (Map.Entry<String, List<AppRuleDTO>> entry : map.entrySet()) {
            String appName = entry.getKey();
            List<Integer> ruleIds = new ArrayList<>();
            for (AppRuleDTO appRuleDTO : entry.getValue()) {
                Integer id = appRuleDTO.getId();
                ruleIds.add(id);
            }

            CopyOnWriteArrayList<AppRuleDTO> ruleDTOS = ROUTE_RULE_MAP.getOrDefault(appName, new CopyOnWriteArrayList<>());

            ruleDTOS.removeIf(r -> ruleIds.contains(r.getId()));

            if (CollectionUtils.isEmpty(ruleDTOS)) {
                // remove all
                ROUTE_RULE_MAP.remove(appName);
            } else {
                // remove some of them
                ROUTE_RULE_MAP.put(appName, ruleDTOS);
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
