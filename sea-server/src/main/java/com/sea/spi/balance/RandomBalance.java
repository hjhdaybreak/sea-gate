package com.sea.spi.balance;

import com.sea.annotation.LoadBalanceAno;
import com.sea.constant.LoadBalanceConstants;
import com.sea.pojo.dto.ServiceInstance;
import com.sea.spi.LoadBalance;


import java.util.List;
import java.util.Random;

@LoadBalanceAno(LoadBalanceConstants.RANDOM)
public class RandomBalance implements LoadBalance {

    private static final Random random = new Random();

    @Override
    public synchronized ServiceInstance chooseOne(List<ServiceInstance> instances) {
        return instances.get(random.nextInt(instances.size()));
    }
}
