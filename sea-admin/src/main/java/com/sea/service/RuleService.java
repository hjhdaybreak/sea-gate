package com.sea.service;


import com.sea.pojo.ChangeStatusDTO;
import com.sea.pojo.RuleDTO;
import com.sea.pojo.dto.AppRuleDTO;
import com.sea.pojo.vo.RuleVO;

import java.util.List;

public interface RuleService {
    List<AppRuleDTO> getEnabledRule();

    void add(RuleDTO ruleDTO);

    void delete(Integer id);

    List<RuleVO> queryList(String appName);

    void changeStatus(ChangeStatusDTO statusDTO);
}
