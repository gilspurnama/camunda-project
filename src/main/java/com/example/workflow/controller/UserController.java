package com.example.workflow.controller;

import com.example.workflow.dto.ResponseDto;
import com.example.workflow.dto.UserDto;
import com.example.workflow.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('md:user:read')")
    public ResponseEntity<ResponseDto<List<UserDto.Response>>> get(){
        return userService.list();
    }
}
