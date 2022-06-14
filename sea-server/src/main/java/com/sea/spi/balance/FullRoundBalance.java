package com.sea.spi.balance;

import com.sea.annotation.LoadBalanceAno;
import com.sea.constant.LoadBalanceConstants;
import com.sea.pojo.dto.ServiceInstance;
import com.sea.spi.LoadBalance;

import java.util.List;

@LoadBalanceAno(LoadBalanceConstants.ROUND)
public class FullRoundBalance implements LoadBalance {
    private volatile int index;

    @Override
    public ServiceInstance chooseOne(List<ServiceInstance> instances) {
        if (index == instances.size()) {
            index = 0;
        }
        return instances.get(index++);
    }
}
