package com.sea.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sea.bean.AppPlugin;
import com.sea.pojo.AppPluginDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AppPluginMapper extends BaseMapper<AppPlugin> {
    List<AppPluginDTO> queryEnabledPlugins(@Param("appIds") List<Integer> appIds);
}
