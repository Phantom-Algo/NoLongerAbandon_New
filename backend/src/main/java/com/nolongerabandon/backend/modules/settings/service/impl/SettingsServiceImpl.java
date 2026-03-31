package com.nolongerabandon.backend.modules.settings.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.nolongerabandon.backend.common.exception.BusinessException;
import com.nolongerabandon.backend.common.util.CryptoUtil;
import com.nolongerabandon.backend.modules.settings.dto.ModelConfigSaveRequest;
import com.nolongerabandon.backend.modules.settings.dto.UserProfileUpdateRequest;
import com.nolongerabandon.backend.modules.settings.entity.ModelConfig;
import com.nolongerabandon.backend.modules.settings.entity.UserProfile;
import com.nolongerabandon.backend.modules.settings.mapper.ModelConfigMapper;
import com.nolongerabandon.backend.modules.settings.mapper.UserProfileMapper;
import com.nolongerabandon.backend.modules.settings.service.SettingsService;
import com.nolongerabandon.backend.modules.settings.vo.ModelConfigVO;
import com.nolongerabandon.backend.modules.settings.vo.UserProfileVO;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class SettingsServiceImpl implements SettingsService {

    private static final int FLAG_TRUE = 1;
    private static final int FLAG_FALSE = 0;

    private final ModelConfigMapper modelConfigMapper;
    private final UserProfileMapper userProfileMapper;

    public SettingsServiceImpl(ModelConfigMapper modelConfigMapper, UserProfileMapper userProfileMapper) {
        this.modelConfigMapper = modelConfigMapper;
        this.userProfileMapper = userProfileMapper;
    }

    @Override
    public List<ModelConfigVO> listModelConfigs() {
        LambdaQueryWrapper<ModelConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(ModelConfig::getIsWordGenerationDefault)
                .orderByDesc(ModelConfig::getId);
        return modelConfigMapper.selectList(queryWrapper)
                .stream()
                .map(this::toModelConfigVO)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ModelConfigVO createModelConfig(ModelConfigSaveRequest request) {
        ModelConfig modelConfig = new ModelConfig();
        modelConfig.setProviderName(request.getProviderName().trim());
        modelConfig.setDisplayName(request.getDisplayName().trim());
        modelConfig.setBaseUrl(normalizeNullableText(request.getBaseUrl()));
        modelConfig.setModelName(request.getModelName().trim());
        modelConfig.setEnabled(booleanToFlag(defaultTrue(request.getEnabled())));
        modelConfig.setIsWordGenerationDefault(booleanToFlag(Boolean.TRUE.equals(request.getWordGenerationDefault())));
        modelConfig.setCompletionsPath(normalizeCompletionsPath(request.getCompletionsPath()));

        if (StringUtils.hasText(request.getApiKey())) {
            modelConfig.setApiKeyEncrypted(CryptoUtil.encrypt(request.getApiKey().trim()));
        }

        if (Boolean.TRUE.equals(request.getWordGenerationDefault())) {
            clearWordGenerationDefaultFlag();
        }

        modelConfigMapper.insert(modelConfig);
        return toModelConfigVO(modelConfigMapper.selectById(modelConfig.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ModelConfigVO updateModelConfig(Long id, ModelConfigSaveRequest request) {
        ModelConfig existingModel = requireModelConfig(id);

        existingModel.setProviderName(request.getProviderName().trim());
        existingModel.setDisplayName(request.getDisplayName().trim());
        existingModel.setBaseUrl(normalizeNullableText(request.getBaseUrl()));
        existingModel.setModelName(request.getModelName().trim());
        existingModel.setEnabled(booleanToFlag(defaultTrue(request.getEnabled())));

        if (StringUtils.hasText(request.getApiKey())) {
            existingModel.setApiKeyEncrypted(CryptoUtil.encrypt(request.getApiKey().trim()));
        }

        existingModel.setCompletionsPath(normalizeCompletionsPath(request.getCompletionsPath()));

        boolean makeDefault = Boolean.TRUE.equals(request.getWordGenerationDefault());
        existingModel.setIsWordGenerationDefault(booleanToFlag(makeDefault));
        if (makeDefault) {
            clearWordGenerationDefaultFlag();
        }

        modelConfigMapper.updateById(existingModel);
        return toModelConfigVO(modelConfigMapper.selectById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteModelConfig(Long id) {
        ModelConfig modelConfig = requireModelConfig(id);
        modelConfigMapper.deleteById(id);

        if (Objects.equals(modelConfig.getIsWordGenerationDefault(), FLAG_TRUE)) {
            applyNewestModelAsDefaultIfMissing();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ModelConfigVO setWordGenerationDefaultModel(Long id) {
        ModelConfig modelConfig = requireModelConfig(id);

        clearWordGenerationDefaultFlag();
        modelConfig.setIsWordGenerationDefault(FLAG_TRUE);
        modelConfigMapper.updateById(modelConfig);

        return toModelConfigVO(modelConfigMapper.selectById(id));
    }

    @Override
    public UserProfileVO getUserProfile() {
        return toUserProfileVO(getOrCreateUserProfile());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserProfileVO updateUserProfile(UserProfileUpdateRequest request) {
        UserProfile userProfile = getOrCreateUserProfile();
        userProfile.setNickname(normalizeNullableText(request.getNickname()));
        userProfile.setProfileMarkdown(normalizeNullableText(request.getProfileMarkdown()));

        if (Objects.nonNull(request.getAllowAiReadProfile())) {
            userProfile.setAllowAiReadProfile(booleanToFlag(request.getAllowAiReadProfile()));
        }

        userProfileMapper.updateById(userProfile);
        return toUserProfileVO(userProfileMapper.selectById(userProfile.getId()));
    }

    private ModelConfig requireModelConfig(Long id) {
        ModelConfig modelConfig = modelConfigMapper.selectById(id);
        if (modelConfig == null) {
            throw new BusinessException("SETTINGS_MODEL_NOT_FOUND", "模型配置不存在");
        }
        return modelConfig;
    }

    private UserProfile getOrCreateUserProfile() {
        LambdaQueryWrapper<UserProfile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(UserProfile::getId).last("LIMIT 1");
        UserProfile existing = userProfileMapper.selectOne(queryWrapper);
        if (existing != null) {
            return existing;
        }

        UserProfile created = new UserProfile();
        created.setAllowAiReadProfile(FLAG_FALSE);
        userProfileMapper.insert(created);
        return userProfileMapper.selectById(created.getId());
    }

    private void clearWordGenerationDefaultFlag() {
        LambdaUpdateWrapper<ModelConfig> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(ModelConfig::getIsWordGenerationDefault, FLAG_FALSE)
                .eq(ModelConfig::getIsWordGenerationDefault, FLAG_TRUE);
        modelConfigMapper.update(null, updateWrapper);
    }

    private void applyNewestModelAsDefaultIfMissing() {
        LambdaQueryWrapper<ModelConfig> defaultQuery = new LambdaQueryWrapper<>();
        defaultQuery.eq(ModelConfig::getIsWordGenerationDefault, FLAG_TRUE).last("LIMIT 1");
        ModelConfig existingDefault = modelConfigMapper.selectOne(defaultQuery);
        if (existingDefault != null) {
            return;
        }

        LambdaQueryWrapper<ModelConfig> latestModelQuery = new LambdaQueryWrapper<>();
        latestModelQuery.orderByDesc(ModelConfig::getId).last("LIMIT 1");
        ModelConfig latest = modelConfigMapper.selectOne(latestModelQuery);
        if (latest == null) {
            return;
        }

        latest.setIsWordGenerationDefault(FLAG_TRUE);
        modelConfigMapper.updateById(latest);
    }

    private ModelConfigVO toModelConfigVO(ModelConfig entity) {
        String apiKeyMasked = null;
        if (StringUtils.hasText(entity.getApiKeyEncrypted())) {
            try {
                String plainApiKey = CryptoUtil.decrypt(entity.getApiKeyEncrypted());
                apiKeyMasked = maskApiKey(plainApiKey);
            } catch (Exception ex) {
                log.warn("模型[{}]的 API Key 解密失败，可能是密钥已变更，请重新设置", entity.getDisplayName(), ex);
                apiKeyMasked = "***解密失败，请重新编辑***";
            }
        }

        return ModelConfigVO.builder()
                .id(entity.getId())
                .providerName(entity.getProviderName())
                .displayName(entity.getDisplayName())
                .baseUrl(entity.getBaseUrl())
                .modelName(entity.getModelName())
                .apiKeyMasked(apiKeyMasked)
                .enabled(flagToBoolean(entity.getEnabled()))
                .wordGenerationDefault(flagToBoolean(entity.getIsWordGenerationDefault()))
                .completionsPath(entity.getCompletionsPath())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private UserProfileVO toUserProfileVO(UserProfile entity) {
        return UserProfileVO.builder()
                .id(entity.getId())
                .nickname(entity.getNickname())
                .profileMarkdown(entity.getProfileMarkdown())
                .allowAiReadProfile(flagToBoolean(entity.getAllowAiReadProfile()))
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private String maskApiKey(String apiKey) {
        if (!StringUtils.hasText(apiKey)) {
            return null;
        }
        if (apiKey.length() <= 8) {
            return "***";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }

    private String normalizeNullableText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    /** 规范化 completionsPath：为空则使用默认值 */
    private String normalizeCompletionsPath(String value) {
        if (!StringUtils.hasText(value)) {
            return "/v1/chat/completions";
        }
        return value.trim();
    }

    private Integer booleanToFlag(Boolean value) {
        return Boolean.TRUE.equals(value) ? FLAG_TRUE : FLAG_FALSE;
    }

    private Boolean flagToBoolean(Integer flag) {
        return Objects.equals(flag, FLAG_TRUE);
    }

    private boolean defaultTrue(Boolean value) {
        return value == null || value;
    }
}
