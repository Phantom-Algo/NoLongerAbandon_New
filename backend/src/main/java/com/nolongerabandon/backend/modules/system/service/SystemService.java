package com.nolongerabandon.backend.modules.system.service;

import com.nolongerabandon.backend.modules.system.vo.SystemStatusVO;

public interface SystemService {

    SystemStatusVO getSystemStatus();
}