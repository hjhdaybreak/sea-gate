package com.sea.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.sea.bean.App;
import com.sea.bean.AppInstance;
import com.sea.mapper.AppInstanceMapper;
import com.sea.mapper.AppMapper;
import com.sea.pojo.UpdateWeightDTO;
import com.sea.pojo.vo.InstanceVO;
import com.sea.service.AppInstanceService;
import com.sea.transfer.InstanceVOTransfer;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class AppInstanceServiceImpl implements AppInstanceService {

    @Resource
    private AppInstanceMapper instanceMapper;

    @Resource
    private AppMapper appMapper;

    @Override
    public List<InstanceVO> queryList(Integer appId) {
        App app = appMapper.selectById(appId);
        QueryWrapper<AppInstance> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(AppInstance::getAppId, appId);
        List<AppInstance> instanceList = instanceMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(instanceList)) {
            return Lists.newArrayList();
        }
        List<InstanceVO> voList = InstanceVOTransfer.INSTANCE.mapToVOS(instanceList);
        for (InstanceVO vo : voList) {
            vo.setAppName(app.getAppName());
        }
        return voList;
    }

    @Override
    public void updateWeight(UpdateWeightDTO updateWeightDTO) {
        AppInstance appInstance = new AppInstance();
        appInstance.setId(updateWeightDTO.getId());
        appInstance.setWeight(updateWeightDTO.getWeight());
        instanceMapper.updateById(appInstance);
    }
}
