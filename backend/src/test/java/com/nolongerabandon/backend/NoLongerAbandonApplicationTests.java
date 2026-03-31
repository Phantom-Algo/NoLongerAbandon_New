package com.nolongerabandon.backend;

import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
class NoLongerAbandonApplicationTests {

    private static final Path TEST_DB_PATH =
            Path.of("target", "test-application-" + UUID.randomUUID() + ".db");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("app.storage.sqlite-path", () -> TEST_DB_PATH.toString());
    }

    @Test
    void contextLoads() {
    }
}