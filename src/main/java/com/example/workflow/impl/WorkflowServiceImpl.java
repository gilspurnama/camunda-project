package com.example.workflow.impl;

import com.example.workflow.dto.*;
import com.example.workflow.exception.ResponseException;
import com.example.workflow.service.WorkflowService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.workflow.util.ExceptionEnum.*;

@Slf4j
@Service
@AllArgsConstructor
public class WorkflowServiceImpl implements WorkflowService {

    private final RepositoryService repositoryService;

    private final RuntimeService runtimeService;

    private final TaskService taskService;

    @Override
    public ResponseEntity<ResponseDto<List<WorkflowDto>>> workflowList(Optional<String> version, Optional<String> status) {
        ProcessDefinitionQuery query =  repositoryService.createProcessDefinitionQuery();
        if (version.isPresent() && version.get().equals("latest")) {
            query.latestVersion();
        }
        if (status.isPresent() && status.get().equals("suspend")) {
            query.suspended();
        } else {
            query.active();
        }
        List<WorkflowDto> responses = query.list().stream().map(workflow ->
                WorkflowDto.builder()
                        .id(workflow.getId())
                        .name(workflow.getName())
                        .key(workflow.getKey())
                        .description(workflow.getDescription())
                        .versionTag(workflow.getVersionTag())
                        .version(workflow.getVersion())
                        .category(workflow.getCategory())
                        .historyTimeToLive(workflow.getHistoryTimeToLive())
                        .resourceName(workflow.getResourceName())
                        .diagramResourceName(workflow.getDiagramResourceName())
                        .deploymentId(workflow.getDeploymentId())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ResponseDto<>(responses));
    }

    @Override
    public ResponseEntity<ResponseDto<List<DeploymentDto>>> deploymentList() {
        List<DeploymentDto> responses = repositoryService.createDeploymentQuery().list()
                .stream()
                .map(workflow -> DeploymentDto.builder().id(workflow.getId())
                        .name(workflow.getName())
                        .deploymentTime(workflow.getDeploymentTime())
                        .source(workflow.getSource())
                        .tenantId(workflow.getTenantId()).build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ResponseDto<>(responses));
    }

    @Override
    public ResponseEntity<ResponseDto<List<WorkflowDto>>> suspendProcessDefinition(RequestDTO requestDTO){
        if (requestDTO.getKey().isEmpty() && requestDTO.getId().isEmpty()) {
            throw new ResponseException(INSERT_KEY_ID.message(), INSERT_KEY_ID.httpStatus(), INSERT_KEY_ID.code());
        }
        if (checkWorkflow(requestDTO).isEmpty()) {
            throw new ResponseException(WORKFLOW_NOT_FOUND.message(), WORKFLOW_NOT_FOUND.httpStatus(), WORKFLOW_NOT_FOUND.code());
        }
        String responseMessage;
        if (requestDTO.getKey().isEmpty()){
            repositoryService.suspendProcessDefinitionById(requestDTO.getId());
            responseMessage = String.format("ID: %s",requestDTO.getId());
        } else {
            repositoryService.suspendProcessDefinitionByKey(requestDTO.getKey());
            responseMessage = String.format("KEY: %s",requestDTO.getKey());
        }
        return ResponseEntity.ok(new ResponseDto<>(null, String.format("successfully suspend workflow with %s",responseMessage)));
    }

    @Override
    public ResponseEntity<ResponseDto<List<WorkflowDto>>> activatesProcessDefinition(RequestDTO requestDTO){
        if (requestDTO.getKey().isEmpty() && requestDTO.getId().isEmpty()) {
            throw new ResponseException(INSERT_KEY_ID.message(), INSERT_KEY_ID.httpStatus(), INSERT_KEY_ID.code());
        }
        if (checkWorkflow(requestDTO).isEmpty()) {
            throw new ResponseException(WORKFLOW_NOT_FOUND.message(), WORKFLOW_NOT_FOUND.httpStatus(), WORKFLOW_NOT_FOUND.code());
        }
        String responseMessage;
        if (requestDTO.getKey().isEmpty()){
            repositoryService.activateProcessDefinitionById(requestDTO.getId());
            responseMessage = String.format("ID: %s",requestDTO.getId());
        } else {
            repositoryService.activateProcessDefinitionByKey(requestDTO.getKey());
            responseMessage = String.format("KEY: %s",requestDTO.getKey());
        }
        return ResponseEntity.ok(new ResponseDto<>(null, String.format("successfully activate workflow with %s",responseMessage)));
    }

    @Override
    public ResponseEntity<ResponseDto<List<WorkflowDto>>> deleteWorkflowVersioning(String workflowId){
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().processDefinitionId(workflowId).list();
        if (list.isEmpty()) {
            throw new ResponseException(WORKFLOW_NOT_FOUND.message(), WORKFLOW_NOT_FOUND.httpStatus(), WORKFLOW_NOT_FOUND.code());
        }
        repositoryService.deleteProcessDefinition(workflowId, true);
        return ResponseEntity.ok(new ResponseDto<>(null, String.format("Successfully delete workflow with ID: %s",workflowId)));
    }

    @Override
    public ResponseEntity<ResponseDto<List<WorkflowDto>>> createDeploymentProcess(MultipartFile document) {
        try {
            repositoryService.createDeployment()
                    .addInputStream(document.getOriginalFilename(), document.getInputStream())
                    .enableDuplicateFiltering(true)
                    .deployWithResult();
            return ResponseEntity.ok(new ResponseDto<>(null, String.format("Successfully deploy workflow with name: %s",document.getName())));
        } catch (Exception e) {
            log.error("Error",e);
            throw new ResponseException(WORKFLOW_WRONG_UPLOAD_FILE.message(), WORKFLOW_WRONG_UPLOAD_FILE.httpStatus(), WORKFLOW_WRONG_UPLOAD_FILE.code());
        }
    }

    @Override
    public ResponseEntity<?> startProcessInstanceByKey(String workflowName, String userId) {
        ProcessInstance startInstance = runtimeService.startProcessInstanceByKey(workflowName);
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(startInstance.getId()).list();
        taskService.claim(tasks.get(0).getId(), userId);

        Map<String, String> response = new HashMap<>();
        response.put("instanceId", startInstance.getId());
        response.put("processDefinitionId", startInstance.getProcessDefinitionId());
        response.put("taskId", tasks.get(0).getId());

        return ResponseEntity.ok(new ResponseDto<>(response));
    }

    private List<ProcessDefinition> checkWorkflow(RequestDTO requestDTO) {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
        if (requestDTO.getKey().isEmpty()){
            query.processDefinitionId(requestDTO.getId());
        } else {
            query.processDefinitionKey(requestDTO.getKey());
        }
        return query.list();
    }
}
