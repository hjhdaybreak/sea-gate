package com.sea.constant;

public enum SeaPluginEnum {

    /**
     * DynamicRoute
     */
    DYNAMIC_ROUTE("DynamicRoute", 2, "动态路由插件"),

    /**
     * Auth
     */
    AUTH("Auth", 1, "鉴权插件");

    private String name;

    private Integer order;

    private String desc;

    SeaPluginEnum(String name, Integer order, String desc) {
        this.name = name;
        this.order = order;
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
