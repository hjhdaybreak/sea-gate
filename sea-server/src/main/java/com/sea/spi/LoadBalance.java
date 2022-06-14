package com.sea.spi;

import com.sea.pojo.dto.ServiceInstance;

import java.util.List;

public interface LoadBalance {

    ServiceInstance chooseOne(List<ServiceInstance> instances);
}
