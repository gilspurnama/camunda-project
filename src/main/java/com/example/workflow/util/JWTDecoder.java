package com.example.workflow.util;

import com.example.workflow.dto.UserDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.Map;

public class JWTDecoder {
    public static UserDto.UserCredential getCredentialFromJWT() {
        Authentication authentication =  SecurityContextHolder.getContext().getAuthentication();
        if(authentication instanceof JwtAuthenticationToken) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) realmAccess.get("roles");
            return UserDto.UserCredential.builder()
                    .userId(jwt.getClaimAsString("sub"))
                    .roleName(roles)
                    .email(jwt.getClaimAsString("email"))
                    .build();
        }
        return null;
    }

}