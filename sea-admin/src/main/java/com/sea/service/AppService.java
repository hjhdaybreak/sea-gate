package com.sea.service;

import com.sea.dto.RegisterAppDTO;
import com.sea.dto.UnregisterAppDTO;

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

}
