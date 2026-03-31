package com.nolongerabandon.backend.common.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.sqlite.JDBC;

class FlywayMigrationTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldInitializeFreshDatabaseWithVersionedMigrations() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(createDataSource(tempDir.resolve("fresh.db")));

        migrate(tempDir.resolve("fresh.db"));

        Integer wordTableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM sqlite_master WHERE type='table' AND name='word'",
                Integer.class);
        Integer flywayHistoryCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM sqlite_master WHERE type='table' AND name='flyway_schema_history'",
                Integer.class);
        Integer migrationCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM flyway_schema_history WHERE success = 1 AND version IS NOT NULL",
            Integer.class);

        assertEquals(1, wordTableCount);
        assertEquals(1, flywayHistoryCount);
        assertEquals(1, migrationCount);
    }

    private void migrate(Path dbPath) {
        Flyway.configure()
                .dataSource(createDataSource(dbPath))
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }

    private SimpleDriverDataSource createDataSource(Path dbPath) {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass(JDBC.class);
        dataSource.setUrl("jdbc:sqlite:" + dbPath);
        return dataSource;
    }
}