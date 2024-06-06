package com.example.workflow.impl;

import com.example.workflow.dto.RequestCreateTaskDto;
import com.example.workflow.dto.ResponseDto;
import com.example.workflow.dto.TaskDto;
import com.example.workflow.exception.ResponseException;
import com.example.workflow.service.TasksService;
import com.example.workflow.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.workflow.util.ExceptionEnum.*;

@Service
@Slf4j
@AllArgsConstructor
public class EmbeddedTasksService implements TasksService {

    private final TaskService taskService;

    private final RuntimeService runtimeService;

    private final IdentityService identityService;

    @Override
    public ResponseEntity<ResponseDto<List<TaskDto.Response>>> get(String processDefinitionId, Integer page, Integer size) {

        if (page < 0) {
            throw new ResponseException(PAGE_START.message(), PAGE_START.httpStatus(), PAGE_START.code());
        }

        if (size < 1 || size > 10000) {
            throw new ResponseException(MINIMUM_MAXIMUM_SIZE.message(), MINIMUM_MAXIMUM_SIZE.httpStatus(), MINIMUM_MAXIMUM_SIZE.code());
        }

        TaskQuery taskQuery = taskService.createTaskQuery();
        if (!StringUtils.isEmpty(processDefinitionId)) {
            taskQuery.processDefinitionId(processDefinitionId);
        }
        List<Task> tasks = taskQuery.listPage(page, size);
        if (CollectionUtils.isEmpty(tasks)) {
            throw new ResponseException(TASK_NOT_FOUND.message(), TASK_NOT_FOUND.httpStatus(), TASK_NOT_FOUND.code());
        }

        List<TaskDto.Response> payload = tasks.stream()
                .flatMap(task -> {
                    List<TaskDto.Activity> activities = Collections.emptyList();
                    if(!StringUtils.isEmpty(task.getExecutionId())){
                        List<String> activeActivityInstances = runtimeService.getActiveActivityIds(task.getExecutionId());
                        if (!CollectionUtils.isEmpty(activeActivityInstances)) {
                            ActivityInstance parentInstance = runtimeService.getActivityInstance(task.getProcessInstanceId());
                            activities = activeActivityInstances.stream()
                                    .map(parentInstance::getActivityInstances)
                                    .filter(data -> data != null && data.length > 0)
                                    .flatMap(Arrays::stream)
                                    .map(activityInstance -> TaskDto.Activity.builder()
                                            .id(activityInstance.getActivityId())
                                            .name(activityInstance.getActivityName())
                                            .build())
                                    .collect(Collectors.toList());
                        }
                    }


                    TaskDto.Response result = TaskDto.Response.builder()
                            .id(task.getId())
                            .processDefinitionId(task.getProcessDefinitionId())
                            .processInstanceId(task.getProcessInstanceId())
                            .isSuspended(task.isSuspended())
                            .assignedUser(task.getAssignee())
                            .activities(activities)
                            .build();
                    return Stream.of(result);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ResponseDto<>(payload));
    }

    @Override
    public ResponseEntity<ResponseDto<TaskDto.Response>> get(String taskId) {
        TaskQuery taskQuery = taskService.createTaskQuery()
                .taskId(taskId);
        List<Task> tasks = taskQuery.list();
        if (CollectionUtils.isEmpty(tasks)) {
            throw new ResponseException(TASK_NOT_FOUND.message(), TASK_NOT_FOUND.httpStatus(), TASK_NOT_FOUND.code());
        }

        Task task = tasks.get(0);
        List<TaskDto.Activity> activities = Collections.emptyList();
        if(!StringUtils.isEmpty(task.getExecutionId())){
            List<String> activeActivityInstances = runtimeService.getActiveActivityIds(task.getExecutionId());
            if (!CollectionUtils.isEmpty(activeActivityInstances)) {
                ActivityInstance parentInstance = runtimeService.getActivityInstance(task.getProcessInstanceId());
                activities = activeActivityInstances.stream()
                        .map(parentInstance::getActivityInstances)
                        .filter(data -> data != null && data.length > 0)
                        .flatMap(Arrays::stream)
                        .map(activityInstance -> TaskDto.Activity.builder()
                                .id(activityInstance.getActivityId())
                                .name(activityInstance.getActivityName())
                                .build())
                        .collect(Collectors.toList());
            }
        }

        TaskDto.Response result = TaskDto.Response.builder()
                .id(task.getId())
                .processDefinitionId(task.getProcessDefinitionId())
                .processInstanceId(task.getProcessInstanceId())
                .assignedUser(task.getAssignee())
                .isSuspended(task.isSuspended())
                .variables(taskService.getVariables(task.getId()))
                .localVariables(taskService.getVariablesLocal(task.getId()))
                .activities(activities)
                .build();

        return ResponseEntity.ok(new ResponseDto<>(result));
    }

    @Override
    public ResponseEntity<?> complete(String taskId, TaskDto.CompleteRequest request){
        List<Task> tasks = taskService.createTaskQuery().taskId(taskId).list();
        if(CollectionUtils.isEmpty(tasks)){
            throw new ResponseException(TASK_NOT_FOUND.message(), TASK_NOT_FOUND.httpStatus(), TASK_NOT_FOUND.code());
        }

        Task task = tasks.get(0);
        if(StringUtils.isEmpty(task.getAssignee())){
            throw new ResponseException(TASK_NOT_ASSIGNED.message(), TASK_NOT_ASSIGNED.httpStatus(), TASK_NOT_ASSIGNED.code());
        }

        if(task.isSuspended()){
            throw new ResponseException(TASK_SUSPENDED.message(), TASK_SUSPENDED.httpStatus(), TASK_SUSPENDED.code());
        }

       taskService.complete(taskId, CollectionUtils.isEmpty(request.getVariables())?null:request.getVariables());
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> setAssignee(String taskId, TaskDto.SetAssigneeRequest assignee) {
        List<Task> tasks = taskService.createTaskQuery().taskId(taskId).list();
        if(CollectionUtils.isEmpty(tasks)){
            throw new ResponseException(TASK_NOT_FOUND.message(), TASK_NOT_FOUND.httpStatus(), TASK_NOT_FOUND.code());
        }

        if(StringUtils.isEmpty(assignee.getUserId())){
            throw new ResponseException(USER_ID_REQUIRED.message(), USER_ID_REQUIRED.httpStatus(), USER_ID_REQUIRED.code());
        }

        List<User> users = identityService.createUserQuery().userId(assignee.getUserId()).list();
        if(users.isEmpty()){
            throw new ResponseException(USER_ID_NOT_FOUND.message(), USER_ID_NOT_FOUND.httpStatus(), USER_ID_NOT_FOUND.code());
        }

        taskService.setAssignee(taskId,users.get(0).getId());
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> setVariables(String taskId, TaskDto.SetVariableRequest variable) {
        List<Task> tasks = taskService.createTaskQuery().taskId(taskId).list();
        if(CollectionUtils.isEmpty(tasks)){
            throw new ResponseException(TASK_NOT_FOUND.message(), TASK_NOT_FOUND.httpStatus(), TASK_NOT_FOUND.code());
        }

        Task task = tasks.get(0);
        if(StringUtils.isEmpty(task.getAssignee())){
            throw new ResponseException(TASK_NOT_ASSIGNED.message(), TASK_NOT_ASSIGNED.httpStatus(), TASK_NOT_ASSIGNED.code());
        }

        if(!CollectionUtils.isEmpty(variable.getVariablesLocal())) {
            taskService.setVariablesLocal(taskId,variable.getVariablesLocal());
        }

        if(!CollectionUtils.isEmpty(variable.getVariables())) {
            taskService.setVariables(taskId,variable.getVariables());
        }

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> createTask(RequestCreateTaskDto requestCreateTaskDto) {
        Task task = taskService.newTask();

        task.setName(requestCreateTaskDto.getTaskName());
        task.setTenantId(requestCreateTaskDto.getTaskTenantId());
        task.setDescription(requestCreateTaskDto.getTaskDescription());
        task.setAssignee(requestCreateTaskDto.getTaskAssignee());
        taskService.saveTask(task);

        Map<String, String> response = new HashMap<>();
        response.put("id", task.getId());
        response.put("name", task.getName());
        response.put("tenantId", task.getTenantId());
        response.put("desccription", task.getDescription());
        response.put("assignee", task.getAssignee());

        return ResponseEntity.ok(new ResponseDto<>(response, "Create New Task Successfully"));
    }

}
