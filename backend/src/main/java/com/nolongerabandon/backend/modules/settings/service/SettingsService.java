package com.nolongerabandon.backend.modules.settings.service;

import com.nolongerabandon.backend.modules.settings.dto.ModelConfigSaveRequest;
import com.nolongerabandon.backend.modules.settings.dto.UserProfileUpdateRequest;
import com.nolongerabandon.backend.modules.settings.vo.ModelConfigVO;
import com.nolongerabandon.backend.modules.settings.vo.UserProfileVO;
import java.util.List;

public interface SettingsService {

    List<ModelConfigVO> listModelConfigs();

    ModelConfigVO createModelConfig(ModelConfigSaveRequest request);

    ModelConfigVO updateModelConfig(Long id, ModelConfigSaveRequest request);

    void deleteModelConfig(Long id);

    ModelConfigVO setWordGenerationDefaultModel(Long id);

    UserProfileVO getUserProfile();

    UserProfileVO updateUserProfile(UserProfileUpdateRequest request);
}
