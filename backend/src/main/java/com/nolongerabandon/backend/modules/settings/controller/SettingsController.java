package com.nolongerabandon.backend.modules.settings.controller;

import com.nolongerabandon.backend.common.api.ApiResponse;
import com.nolongerabandon.backend.modules.settings.dto.ModelConfigSaveRequest;
import com.nolongerabandon.backend.modules.settings.dto.UserProfileUpdateRequest;
import com.nolongerabandon.backend.modules.settings.service.SettingsService;
import com.nolongerabandon.backend.modules.settings.vo.ModelConfigVO;
import com.nolongerabandon.backend.modules.settings.vo.UserProfileVO;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping("/models")
    public ApiResponse<List<ModelConfigVO>> listModels() {
        return ApiResponse.success(settingsService.listModelConfigs());
    }

    @PostMapping("/models")
    public ApiResponse<ModelConfigVO> createModel(@Valid @RequestBody ModelConfigSaveRequest request) {
        return ApiResponse.success(settingsService.createModelConfig(request));
    }

    @PutMapping("/models/{id}")
    public ApiResponse<ModelConfigVO> updateModel(
            @PathVariable Long id,
            @Valid @RequestBody ModelConfigSaveRequest request) {
        return ApiResponse.success(settingsService.updateModelConfig(id, request));
    }

    @DeleteMapping("/models/{id}")
    public ApiResponse<Void> deleteModel(@PathVariable Long id) {
        settingsService.deleteModelConfig(id);
        return ApiResponse.success("删除成功", null);
    }

    @PutMapping("/models/{id}/word-generation-default")
    public ApiResponse<ModelConfigVO> setWordGenerationDefault(@PathVariable Long id) {
        return ApiResponse.success(settingsService.setWordGenerationDefaultModel(id));
    }

    @GetMapping("/profile")
    public ApiResponse<UserProfileVO> getProfile() {
        return ApiResponse.success(settingsService.getUserProfile());
    }

    @PutMapping("/profile")
    public ApiResponse<UserProfileVO> updateProfile(@RequestBody UserProfileUpdateRequest request) {
        return ApiResponse.success(settingsService.updateUserProfile(request));
    }
}
