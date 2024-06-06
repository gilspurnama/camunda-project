package com.example.workflow.service;

import com.example.workflow.dto.ProcessInstanceDto;
import com.example.workflow.dto.ProcessInstanceVariableDto;
import com.example.workflow.dto.ResponseDto;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ProcessService {

    ResponseEntity<ResponseDto<List<ProcessInstanceDto.Response>>> list(Integer page, Integer size);

    ResponseEntity<ResponseDto<ProcessInstanceDto.Response>> get(String id);

    ResponseEntity<ResponseDto<List<ProcessInstanceVariableDto.Response>>> getOutputs(String id);

}
