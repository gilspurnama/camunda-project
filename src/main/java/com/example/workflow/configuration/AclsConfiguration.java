package com.example.workflow.configuration;

import com.example.workflow.configuration.properties.AclsProperties;
import com.example.workflow.dto.redis.RedisSerializers;
import com.example.workflow.exception.ResponseException;
import com.example.workflow.security.SecurityConfig;
import com.example.workflow.util.RestTemplateResponseNonErrorHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.util.DefaultUriBuilderFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.workflow.util.ExceptionEnum.*;

@Configuration
@ConditionalOnProperty(name = "acls.server")
@EnableConfigurationProperties(AclsProperties.class)
public class AclsConfiguration {

    private static final String EMAIL_CLAIM = "email";


    public static class AclsAuthenticationFilter extends OncePerRequestFilter {

        private final Client client;

        private final AntPathMatcher antPathMatcher;

        private final ObjectMapper objectMapper;

        private final RedisTemplate<String, RedisSerializers.RolePermission> redisTemplate;

        private final HandlerExceptionResolver handlerExceptionResolver;

        public AclsAuthenticationFilter(AclsProperties aclsProperties,RedisTemplate<String,RedisSerializers.RolePermission> redisTemplate,HandlerExceptionResolver handlerExceptionResolver){
            RestTemplate restTemplate = new RestTemplateBuilder()
                    .build();
            this.objectMapper = new ObjectMapper();
            restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(aclsProperties.getServer()));
            restTemplate.setErrorHandler(new RestTemplateResponseNonErrorHandler());
            client = new Client(restTemplate,aclsProperties);
            antPathMatcher = new AntPathMatcher();
            antPathMatcher.setCachePatterns(true);
            this.redisTemplate = redisTemplate;
            this.handlerExceptionResolver = handlerExceptionResolver;
        }


        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication instanceof JwtAuthenticationToken) {
                    Jwt jwt = (Jwt) authentication.getPrincipal();
                    String email = jwt.getClaimAsString(EMAIL_CLAIM);
                    RedisSerializers.RolePermission rolePermission = redisTemplate.opsForValue().get(email);
                    do {
                       if (rolePermission == null) {
                            ResponseEntity<String> responseEntity = client.getAccessControl(request.getHeader(HttpHeaders.AUTHORIZATION));
                            if (responseEntity.getStatusCode().isError()) {
                                if(responseEntity.getStatusCode().value() == HttpStatus.NOT_FOUND.value()){
                                    if (Objects.requireNonNull(responseEntity.getBody()).contains("400001")) {
                                        throw new ResponseException(USER_NOT_FOUND.message(), USER_NOT_FOUND.httpStatus(), UNAUTHORIZED.code());
                                    }
                                    throw new ResponseException(USER_NOT_ASSIGN_TO_ROLE.message(), USER_NOT_ASSIGN_TO_ROLE.httpStatus(), USER_NOT_ASSIGN_TO_ROLE.code());
                                }
                                throw new ResponseException(responseEntity.getBody(), HttpStatus.valueOf(responseEntity.getStatusCode().value()), UNKNOWN_ERROR.code());
                            }

                            AccessControl accessControl = objectMapper.readValue(responseEntity.getBody(), AccessControlGetResponseDto.class).getPayload();
                            RedisSerializers.RolePermission.Builder role = RedisSerializers.RolePermission.newBuilder();
                            Map<String,Set<String>> methodPaths = new HashMap<>();
                            accessControl.getPermissions().forEach((roleName, permission) ->{
                                role.putRoleTimestamp(roleName, permission.updatedTimestamp);
                                if(permission.getPermission() == null){
                                    return;
                                }
                                permission.getPermission().forEach((method, paths)->
                                    methodPaths.computeIfAbsent(method,$->new HashSet<>()).addAll(paths)
                                );
                            });
                            methodPaths.forEach((method,paths)->role.putPaths(method, RedisSerializers.PathValue.newBuilder().addAllValues(paths).build()));
                            rolePermission = role.build();
                            redisTemplate.opsForValue().set(email, rolePermission);
                        } else{
                           List<String> roles = new ArrayList<>(rolePermission.getRoleTimestampMap().keySet());
                           String text = roles.stream().collect(Collectors.joining(","));
                           ResponseEntity<?> responseEntity = client.getCache(new String(Base64.getUrlEncoder().encode(text.getBytes())),request.getHeader(HttpHeaders.AUTHORIZATION));
                           if(responseEntity.getStatusCode().is2xxSuccessful()) {
                               List<Long> cacheControl = Arrays.stream(responseEntity.getHeaders().getCacheControl().split(",")).map(Long::parseLong).collect(Collectors.toList());
                               for(int i = 0;i < cacheControl.size();i++){
                                   if(rolePermission.getRoleTimestampMap().get(roles.get(i)).longValue() != cacheControl.get(i).longValue()){
                                       rolePermission = null;
                                       break;
                                   }
                               }
                           }
                        }
                    } while(rolePermission == null);

                    RedisSerializers.PathValue pathValue = rolePermission.getPathsMap().get(request.getMethod());
                    if(pathValue == null || CollectionUtils.isEmpty(pathValue.getValuesList()) || !pathValue.getValuesList().stream().anyMatch(pathPattern-> antPathMatcher.match(pathPattern,request.getRequestURI()))){
                        throw new AccessDeniedException("Forbidden");
                    }
                }

                filterChain.doFilter(request, response);
            }
            catch (Exception e){
                handlerExceptionResolver.resolveException(request,response,null,e);
            }
        }
    }

    @Bean
    public SecurityConfig.SecurityCustomizer aclsAuthenticationFilter(AclsProperties aclsProperties
            , RedisTemplate<String,RedisSerializers.RolePermission> redisTemplate
            , HandlerExceptionResolver handlerExceptionResolver){
        return httpSecurity -> {
            Filter filter = new AclsAuthenticationFilter(aclsProperties, redisTemplate, handlerExceptionResolver);
            httpSecurity.addFilterAfter(filter, BearerTokenAuthenticationFilter.class);
        };
    }

    public static class Client{


        private final String ACCESS_CONTROL_URL = "/access-control";

        private final RestTemplate restTemplate;

        private final AclsProperties aclsProperties;

        public Client(RestTemplate restTemplate,AclsProperties aclsProperties) {
            this.restTemplate = restTemplate;
            this.aclsProperties = aclsProperties;
        }

        public ResponseEntity<?> getCache(String roleId,String token){
            RequestEntity<?> request = RequestEntity.head(URI.create(aclsProperties.getServer()+ACCESS_CONTROL_URL+"/"+roleId)).header(HttpHeaders.AUTHORIZATION,token).build();
            return restTemplate.exchange(request,Object.class);
        }


        public ResponseEntity<String> getAccessControl(String token){
            RequestEntity<?> request = RequestEntity.get(URI.create(aclsProperties.getServer()+ACCESS_CONTROL_URL)).header(HttpHeaders.AUTHORIZATION,token).build();
            return restTemplate.exchange(request,String.class);
        }
    }


    @Data
    public static class AccessControlGetResponseDto{
        private AccessControl payload;
    }

    @Data
    public static class AccessControl{
        private Map<String, Permission> permissions;
    }

    @Data
    public static class Permission{
        private Map<String, Set<String>> permission;
        private Long updatedTimestamp;
    }

}
