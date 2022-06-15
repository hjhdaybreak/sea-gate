package com.sea.service;

import com.sea.pojo.UpdateWeightDTO;
import com.sea.pojo.vo.InstanceVO;

import java.util.List;


public interface AppInstanceService {

    List<InstanceVO> queryList(Integer appId);

    void updateWeight(UpdateWeightDTO updateWeightDTO);

}
