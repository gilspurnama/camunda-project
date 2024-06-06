package com.example.workflow.impl;

import com.example.workflow.dto.ResponseDto;
import com.example.workflow.dto.UserDto;
import com.example.workflow.service.UserService;
import lombok.AllArgsConstructor;
import org.camunda.bpm.engine.IdentityService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EmbeddedUserService implements UserService {

    private final IdentityService identityService;

    @Override
    public ResponseEntity<ResponseDto<List<UserDto.Response>>> list() {
        List<UserDto.Response> responses = identityService.createUserQuery().list().stream().map(user ->
                UserDto.Response.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail()).build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ResponseDto<>(responses));
    }

}
