package by.mluvse.UserService.controllerTests;

import by.mluvse.UserService.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379)
            .waitingFor(Wait.forLogMessage(".*Ready to accept connections.*\\n", 1))
            .withStartupTimeout(Duration.ofSeconds(60));


    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired(required = false)
    protected CacheManager cacheManager;

    @Autowired(required = false)
    private RedisConnectionFactory redisConnectionFactory;

    @BeforeEach
    void cleanUp() {
        userRepository.deleteAll();

        if (cacheManager != null && isRedisAvailable()) {
            try {
                cacheManager.getCacheNames().forEach(name -> {
                    Optional.ofNullable(cacheManager.getCache(name))
                            .ifPresent(Cache::clear);
                });
            } catch (Exception e) {
                // Игнорируем ошибки очистки кэша
            }
        }
    }

    protected boolean isRedisAvailable() {
        if (redisConnectionFactory == null) return false;
        try {
            redisConnectionFactory.getConnection().ping();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean isUserCached(Long userId) {
        if (!isRedisAvailable()) return false;
        try {
            Cache cache = cacheManager.getCache("users");
            return cache != null && cache.get(userId) != null;
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean isCardCached(Long cardId) {
        if (!isRedisAvailable()) return false;
        try {
            Cache cache = cacheManager.getCache("payment_cards");
            return cache != null && cache.get(cardId) != null;
        } catch (Exception e) {
            return false;
        }
    }
}