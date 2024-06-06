package com.example.workflow.security;

import com.example.workflow.util.JwtKeycloakGrantedAuthoritiesConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, List<SecurityCustomizer> securityCustomizers) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .antMatchers("/processes/**","/tasks/**","/users/**","/webhook/**","/workflows/**").authenticated()
                        .anyRequest().permitAll()
                )
                .csrf().disable()
                .oauth2ResourceServer((oauth2) -> oauth2
                        .jwt(jwtConfigurer ->
                        jwtConfigurer.jwtAuthenticationConverter(new JwtKeycloakGrantedAuthoritiesConverter())));

        securityCustomizers.forEach(securityCustomizer -> securityCustomizer.customize(http));
        return http.build();
    }

    public interface SecurityCustomizer{
        void customize(HttpSecurity http);
    }
}
