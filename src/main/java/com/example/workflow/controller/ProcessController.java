package com.example.workflow.controller;

import com.example.workflow.dto.ProcessInstanceDto;
import com.example.workflow.dto.ProcessInstanceVariableDto;
import com.example.workflow.dto.ResponseDto;
import com.example.workflow.service.ProcessService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@OpenAPIDefinition(info = @Info(title = "API Collection of Processes",description = "collection of methods, to support all process and instance"))
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/processes")
public class ProcessController {

    private final ProcessService processService;

    @GetMapping("/instances")
    public ResponseEntity<ResponseDto<List<ProcessInstanceDto.Response>>> list(
            @Schema(description = "page is required, min value is 0")
            @RequestParam("page") Integer page,
            @Schema(description = "size is required, min value is 1")
            @RequestParam("size") Integer size){
        return processService.list(page,size);
    }

    @GetMapping("/instances/{id}")
    public ResponseEntity<ResponseDto<ProcessInstanceDto.Response>> get(@PathVariable("id") String id){
        return processService.get(id);
    }

    @GetMapping("/instances/{id}/outputs")
    public ResponseEntity<ResponseDto<List<ProcessInstanceVariableDto.Response>>> getOutput(@PathVariable("id") String id){
        return processService.getOutputs(id);
    }
}
