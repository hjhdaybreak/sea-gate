package com.sea.service;


import com.sea.pojo.dto.AppRuleDTO;

import java.util.List;

public interface RuleService {
    List<AppRuleDTO> getEnabledRule();
}