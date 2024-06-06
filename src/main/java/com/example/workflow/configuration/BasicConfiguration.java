package com.example.workflow.configuration;

import com.example.workflow.configuration.properties.SettingProperties;
import com.example.workflow.dto.cache.Hook;
import com.example.workflow.dto.redis.RedisSerializerCollections;
import com.example.workflow.dto.redis.RedisSerializers;
import com.example.workflow.util.CacheManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.web.client.RestTemplate;
import sendinblue.ApiClient;

import static java.time.Duration.ofMinutes;

@Slf4j
@Configuration
@EnableConfigurationProperties(SettingProperties.class)
public class BasicConfiguration {

    @Bean
    public ApiClient apiClient(SettingProperties settingProperties) {
        ApiClient apiClient = sendinblue.Configuration.getDefaultApiClient();
        apiClient.setApiKey(settingProperties.getEmail().getApiKey());
        return apiClient;
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

    @Bean
    public RedisTemplate<String, RedisSerializers.RolePermission> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String,RedisSerializers.RolePermission> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        redisTemplate.setValueSerializer(RedisSerializerCollections.ROLE_PERMISSION_REDIS_SERIALIZER);
        redisTemplate.setHashValueSerializer(RedisSerializerCollections.ROLE_PERMISSION_REDIS_SERIALIZER);
        return redisTemplate;
    }

    @Bean
    public CacheManager.Cache<String, Hook> hookCache(){
        CacheManager cacheManager = CacheManager.getInstance();
        cacheManager.setCleanerInterval(ofMinutes(30).toMillis());
        return cacheManager.newInstance("hook",ofMinutes(30).toMillis());
    }
}
