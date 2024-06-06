package com.example.workflow.dto.cache;

import com.github.mustachejava.Mustache;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Hook {
    private String hash;
    private String url;
    private HttpHeaders header;
    private Mustache mustache;
    private HttpMethod method;
}