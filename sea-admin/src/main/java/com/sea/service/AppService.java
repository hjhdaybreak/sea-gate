package com.sea.service;

import com.sea.pojo.ChangeStatusDTO;
import com.sea.pojo.vo.AppVO;
import com.sea.pojo.dto.AppInfoDTO;
import com.sea.pojo.dto.RegisterAppDTO;
import com.sea.pojo.dto.UnregisterAppDTO;

import java.util.List;

public interface AppService {
    /**
     * register app
     *
     * @param registerAppDTO
     */
    void register(RegisterAppDTO registerAppDTO);

    /**
     * unregister app
     *
     * @param unregisterAppDTO
     */
    void unregister(UnregisterAppDTO unregisterAppDTO);


    List<AppInfoDTO> getAppInfos(List<String> appNames);

    List<AppVO> getList();

    void updateEnabled(ChangeStatusDTO statusDTO);

    void delete(Integer id);
}
