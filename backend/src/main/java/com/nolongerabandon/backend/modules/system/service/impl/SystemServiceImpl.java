package com.nolongerabandon.backend.modules.system.service.impl;

import com.nolongerabandon.backend.modules.system.service.SystemService;
import com.nolongerabandon.backend.modules.system.vo.SystemStatusVO;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class SystemServiceImpl implements SystemService {

    private final Environment environment;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${app.storage.sqlite-path}")
    private String sqlitePath;

    public SystemServiceImpl(Environment environment) {
        this.environment = environment;
    }

    @Override
    public SystemStatusVO getSystemStatus() {
        return SystemStatusVO.builder()
                .applicationName(applicationName)
                .activeProfiles(Arrays.asList(environment.getActiveProfiles()))
                .storagePath(sqlitePath)
                .build();
    }
}