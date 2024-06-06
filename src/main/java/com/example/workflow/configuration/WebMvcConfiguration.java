package com.example.workflow.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final List<HandlerInterceptor> interceptors;

    public void addInterceptors(InterceptorRegistry registry) {
        if(CollectionUtils.isEmpty(interceptors)){
            return;
        }

        interceptors.forEach(registry::addInterceptor);
    }
}
