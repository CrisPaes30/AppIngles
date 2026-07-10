package com.englishmemory;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for integration tests that require a real database connection.
 *
 * Prerequisites:
 *   - Oracle XE running on localhost:1521 (service XEPDB1)
 *   - User 'englishmemory' with CONNECT + RESOURCE grants
 *   - Liquibase migrations applied (automatic on context startup)
 *
 * Each test runs in a transaction that is rolled back after execution,
 * keeping the database clean between tests.
 */
@SpringBootTest
@ActiveProfiles("dev")
@Transactional
@TestPropertySource(properties = {
    "app.ai.provider=mock",
    "logging.level.org.hibernate.SQL=DEBUG",
})
public abstract class AbstractIntegrationTest {
    // Subclasses inject repositories and services via @Autowired
}
