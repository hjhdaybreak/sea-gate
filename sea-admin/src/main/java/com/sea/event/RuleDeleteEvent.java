package com.sea.event;

import com.sea.pojo.dto.AppRuleDTO;
import org.springframework.context.ApplicationEvent;

public class RuleDeleteEvent extends ApplicationEvent {
    private AppRuleDTO appRuleDTO;

    public RuleDeleteEvent(Object source, AppRuleDTO appRuleDTO) {
        super(source);
        this.appRuleDTO = appRuleDTO;
    }

    public AppRuleDTO getAppRuleDTO() {
        return appRuleDTO;
    }
}
