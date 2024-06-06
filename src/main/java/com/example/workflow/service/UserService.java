package com.example.workflow.service;

import com.example.workflow.dto.ResponseDto;
import com.example.workflow.dto.UserDto;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserService {

    ResponseEntity<ResponseDto<List<UserDto.Response>>> list();
}
