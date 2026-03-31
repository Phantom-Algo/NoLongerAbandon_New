package com.nolongerabandon.backend.modules.settings;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SettingsControllerTest {

        private static final Path TEST_DB_PATH =
                        Path.of("target", "test-settings-" + UUID.randomUUID() + ".db");

        @DynamicPropertySource
        static void registerProperties(DynamicPropertyRegistry registry) {
                registry.add("app.storage.sqlite-path", () -> TEST_DB_PATH.toString());
        }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.execute("DELETE FROM model_config");
        jdbcTemplate.execute("DELETE FROM user_profile");
    }

    @Test
    void shouldKeepSingleWordGenerationDefaultModel() throws Exception {
        Map<String, Object> firstRequest = new HashMap<>();
        firstRequest.put("providerName", "openai");
        firstRequest.put("displayName", "OpenAI 4.1");
        firstRequest.put("baseUrl", "https://api.openai.com");
        firstRequest.put("modelName", "gpt-4.1-mini");
        firstRequest.put("apiKey", "sk-first-test-123456");
        firstRequest.put("enabled", true);
        firstRequest.put("wordGenerationDefault", true);

        mockMvc.perform(post("/api/settings/models")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.wordGenerationDefault").value(true))
                .andExpect(jsonPath("$.data.apiKeyMasked").value("sk-f****3456"));

        Map<String, Object> secondRequest = new HashMap<>();
        secondRequest.put("providerName", "openai");
        secondRequest.put("displayName", "OpenAI 4.1 Nano");
        secondRequest.put("baseUrl", "https://api.openai.com");
        secondRequest.put("modelName", "gpt-4.1-nano");
        secondRequest.put("apiKey", "sk-second-test-123456");
        secondRequest.put("enabled", true);
        secondRequest.put("wordGenerationDefault", true);

        mockMvc.perform(post("/api/settings/models")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.wordGenerationDefault").value(true));

        mockMvc.perform(get("/api/settings/models"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].wordGenerationDefault").value(true))
                .andExpect(jsonPath("$.data[0].modelName").value("gpt-4.1-nano"))
                .andExpect(jsonPath("$.data[1].wordGenerationDefault").value(false));

        String encryptedValue = jdbcTemplate.queryForObject(
                "SELECT api_key_encrypted FROM model_config WHERE model_name = ?",
                String.class,
                "gpt-4.1-mini");
        org.junit.jupiter.api.Assertions.assertNotNull(encryptedValue);
        org.junit.jupiter.api.Assertions.assertNotEquals("sk-first-test-123456", encryptedValue);
    }

    @Test
    void shouldDeleteModelConfig() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("providerName", "openai");
        request.put("displayName", "ToDelete");
        request.put("baseUrl", "https://api.openai.com");
        request.put("modelName", "gpt-4.1-mini");
        request.put("apiKey", "sk-delete-test-123456");
        request.put("enabled", true);
        request.put("wordGenerationDefault", false);

        mockMvc.perform(post("/api/settings/models")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Long id = jdbcTemplate.queryForObject(
                "SELECT id FROM model_config WHERE model_name = ?",
                Long.class,
                "gpt-4.1-mini");

        mockMvc.perform(delete("/api/settings/models/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM model_config WHERE id = ?",
                Integer.class,
                id);
        org.junit.jupiter.api.Assertions.assertEquals(0, count);
    }

    @Test
    void shouldReadAndUpdateUserProfile() throws Exception {
        mockMvc.perform(get("/api/settings/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.allowAiReadProfile").value(false));

        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("nickname", "Jintao");
        updateRequest.put("profileMarkdown", "- 关注英语学习\n- 偏好简洁解释");
        updateRequest.put("allowAiReadProfile", true);

        mockMvc.perform(put("/api/settings/profile")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("Jintao"))
                .andExpect(jsonPath("$.data.allowAiReadProfile").value(true));

        mockMvc.perform(get("/api/settings/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("Jintao"))
                .andExpect(jsonPath("$.data.allowAiReadProfile").value(true));
    }
}
