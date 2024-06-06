package com.example.workflow.service;

import com.example.workflow.dto.RequestCreateTaskDto;
import com.example.workflow.dto.ResponseDto;
import com.example.workflow.dto.TaskDto;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface TasksService {

    ResponseEntity<ResponseDto<List<TaskDto.Response>>> get(String processDefinitionId,Integer page,Integer size);

    ResponseEntity<ResponseDto<TaskDto.Response>> get(String taskId);

    ResponseEntity<?> complete(String taskId, TaskDto.CompleteRequest request);

    ResponseEntity<?> setAssignee(String taskId, TaskDto.SetAssigneeRequest assignee);

    ResponseEntity<?> setVariables(String taskId, TaskDto.SetVariableRequest variable);

    ResponseEntity<?> createTask(RequestCreateTaskDto requestCreateTaskDto);
}
