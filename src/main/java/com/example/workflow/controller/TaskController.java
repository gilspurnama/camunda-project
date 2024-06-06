package com.example.workflow.controller;

import com.example.workflow.dto.RequestCreateTaskDto;
import com.example.workflow.dto.ResponseDto;
import com.example.workflow.dto.TaskDto;
import com.example.workflow.service.TasksService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@OpenAPIDefinition(info = @Info(title = "API Collection of Task",description = "collection of methods, to support all actvitiy related to task"))
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/tasks")
public class TaskController {

    private final TasksService tasksService;

    @Operation(description = "get all active tasks(included suspended one)")
    @GetMapping
    public ResponseEntity<ResponseDto<List<TaskDto.Response>>> get(
            @Schema(description = "this is optional, filter by specific processDefinitionId")
            @RequestParam(name = "processDefinitionId",required = false) String processDefinitionId,
            @Schema(description = "page is required, min value is 0")
            @RequestParam(name="page") Integer page,
            @Schema(description = "size is required, min value is 1")
            @RequestParam(name="size") Integer size){
        return tasksService.get(processDefinitionId,page,size);
    }

    @Operation(description = "get detail of an active task, if given id does not exists, will return 404")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<TaskDto.Response>> get(@PathVariable("id") String id){
        return tasksService.get(id);
    }

    @Operation(description = "complete a task with given id, task must be assigned to an user,otherwise error will be raised")
    @PostMapping("/{id}/complete")
    public ResponseEntity<?> complete(@PathVariable("id") String id,
                                      @RequestBody TaskDto.CompleteRequest requestDto){
        return tasksService.complete(id,requestDto);
    }

    @Operation(description = "assign a task with given id,check out user service for getting list user")
    @PostMapping("/{id}/assignee")
    public ResponseEntity<?> setAssignee(@PathVariable("id") String id,@RequestBody TaskDto.SetAssigneeRequest requestDto){
        return tasksService.setAssignee(id,requestDto);
    }

    @Operation(description = "set variables on a task")
    @PostMapping("/{id}/variables")
    public ResponseEntity<?> complete(@PathVariable("id") String id,@RequestBody TaskDto.SetVariableRequest requestDto){
        return tasksService.setVariables(id,requestDto);
    }

    @Operation(description = "create new task")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody RequestCreateTaskDto requestCreateTaskDto) {
        return tasksService.createTask(requestCreateTaskDto);
    }
}
