package com.example.workflow.util;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class JwtKeycloakGrantedAuthoritiesConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    private String principalClaimName = JwtClaimNames.SUB;

    @Override
    public final AbstractAuthenticationToken convert(Jwt jwt) {
        List<GrantedAuthority> authorities = (List<GrantedAuthority>) this.jwtGrantedAuthoritiesConverter.convert(jwt);
        proceedRoleAuthorities(jwt,authorities);
        String principalClaimValue = jwt.getClaimAsString(this.principalClaimName);
        return new JwtAuthenticationToken(jwt, authorities, principalClaimValue);
    }

    private void proceedRoleAuthorities(Jwt jwt,List<GrantedAuthority> grantedAuthorities){
        Map<String,Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if(CollectionUtils.isEmpty(realmAccess)){
            return;
        }

        Object roles = realmAccess.get("roles");
        if(roles != null){
            ((List<String>) roles)
                    .forEach(role->
                    grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_"+role)));
        }
    }

}