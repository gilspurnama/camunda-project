package com.example.workflow.service;

import com.example.workflow.dto.DeploymentDto;
import com.example.workflow.dto.RequestDTO;
import com.example.workflow.dto.ResponseDto;
import com.example.workflow.dto.WorkflowDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface WorkflowService {

    ResponseEntity<ResponseDto<List<WorkflowDto>>> workflowList(Optional<String> version, Optional<String> status);

    ResponseEntity<ResponseDto<List<DeploymentDto>>> deploymentList();

    ResponseEntity<ResponseDto<List<WorkflowDto>>> suspendProcessDefinition(RequestDTO requestDTO);

    ResponseEntity<ResponseDto<List<WorkflowDto>>> activatesProcessDefinition(RequestDTO requestDTO);

    ResponseEntity<ResponseDto<List<WorkflowDto>>> deleteWorkflowVersioning(String deploymentId);

    ResponseEntity<ResponseDto<List<WorkflowDto>>> createDeploymentProcess(MultipartFile document);

    ResponseEntity<?> startProcessInstanceByKey(String workflowName, String userId);
}
