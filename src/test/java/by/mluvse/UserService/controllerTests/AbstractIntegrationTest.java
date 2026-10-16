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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

@SpringBootTest(properties = {
        "spring.docker.compose.enabled=false",
        "spring.cache.type=redis",
        "spring.data.redis.timeout=5s",
        "spring.data.redis.connect-timeout=5s"
})
@AutoConfigureMockMvc
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379)
            .withReuse(true);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.data.redis.password", () -> "");
    }

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

        // Очищаем кэш только если Redis доступен
        if (cacheManager != null && isRedisAvailable()) {
            cacheManager.getCacheNames().forEach(name -> {
                Optional.ofNullable(cacheManager.getCache(name))
                        .ifPresent(Cache::clear);
            });
        }
    }

    private boolean isRedisAvailable() {
        try {
            if (redisConnectionFactory == null) return false;
            redisConnectionFactory.getConnection().ping();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean isUserCached(Long userId) {
        if (cacheManager == null || !isRedisAvailable()) return false;
        try {
            Cache cache = cacheManager.getCache("users");
            return cache != null && cache.get(userId) != null;
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean isCardCached(Long cardId) {
        if (cacheManager == null || !isRedisAvailable()) return false;
        try {
            Cache cache = cacheManager.getCache("payment_cards");
            return cache != null && cache.get(cardId) != null;
        } catch (Exception e) {
            return false;
        }
    }
}
