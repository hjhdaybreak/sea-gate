package com.sea.pojo.dto;

import com.sea.constant.OperationTypeEnum;

import java.util.List;


public class RouteRuleOperationDTO {

    private String operationType;

    private List<AppRuleDTO> ruleList;

    public RouteRuleOperationDTO() {

    }

    public RouteRuleOperationDTO(OperationTypeEnum operationTypeEnum, List<AppRuleDTO> ruleList) {
        this.operationType = operationTypeEnum.getCode();
        this.ruleList = ruleList;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public List<AppRuleDTO> getRuleList() {
        return ruleList;
    }

    public void setRuleList(List<AppRuleDTO> ruleList) {
        this.ruleList = ruleList;
    }
}
