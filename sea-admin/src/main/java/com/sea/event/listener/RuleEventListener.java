package com.sea.event.listener;

import com.sea.event.RuleAddEvent;
import com.sea.event.RuleDeleteEvent;
import com.google.common.collect.Lists;
import com.sea.constant.OperationTypeEnum;
import com.sea.pojo.dto.RouteRuleOperationDTO;
import com.sea.sync.WebsocketSyncCacheClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class RuleEventListener {

    @Autowired
    private WebsocketSyncCacheClient client;

    @EventListener
    public void onAdd(RuleAddEvent ruleAddEvent) {
        RouteRuleOperationDTO operationDTO = new RouteRuleOperationDTO(OperationTypeEnum.INSERT, Lists.newArrayList(ruleAddEvent.getAppRuleDTO()));
        client.send(operationDTO);
    }

    @EventListener
    public void onDelete(RuleDeleteEvent ruleDeleteEvent) {
        RouteRuleOperationDTO operationDTO = new RouteRuleOperationDTO(OperationTypeEnum.DELETE, Lists.newArrayList(ruleDeleteEvent.getAppRuleDTO()));
        client.send(operationDTO);
    }

}
