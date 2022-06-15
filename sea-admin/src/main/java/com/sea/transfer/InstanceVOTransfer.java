package com.sea.transfer;

import com.sea.bean.AppInstance;
import com.sea.pojo.vo.InstanceVO;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

public interface InstanceVOTransfer {

    InstanceVOTransfer INSTANCE = Mappers.getMapper(InstanceVOTransfer.class);

    @Mappings({
            @Mapping(target = "createdTime", expression = "java(com.sea.utils.DateUtils.formatToYYYYMMDDHHmmss(appInstance.getCreatedTime()))")
    })
    InstanceVO mapToVO(AppInstance appInstance);

    List<InstanceVO> mapToVOS(List<AppInstance> appInstances);
}
