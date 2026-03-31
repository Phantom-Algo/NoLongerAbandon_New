package com.nolongerabandon.backend.modules.system.controller;

import com.nolongerabandon.backend.common.api.ApiResponse;
import com.nolongerabandon.backend.modules.system.service.SystemService;
import com.nolongerabandon.backend.modules.system.vo.SystemStatusVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    private final SystemService systemService;

    public SystemController(SystemService systemService) {
        this.systemService = systemService;
    }

    @GetMapping("/status")
    public ApiResponse<SystemStatusVO> status() {
        return ApiResponse.success(systemService.getSystemStatus());
    }
}