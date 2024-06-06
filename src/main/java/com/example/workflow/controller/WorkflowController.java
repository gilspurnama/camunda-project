package com.example.workflow.controller;

import com.example.workflow.dto.DeploymentDto;
import com.example.workflow.dto.RequestDTO;
import com.example.workflow.dto.ResponseDto;
import com.example.workflow.dto.WorkflowDto;
import com.example.workflow.service.WorkflowService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;

@OpenAPIDefinition(info = @Info(title = "API Collection of Workflow",description = "collection of methods, to support all actvitiy related to workflow"))
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/workflows")
public class WorkflowController {

    @Autowired
    private WorkflowService workflowService;

    @Operation(description = "get all workflow")
    @GetMapping
    public ResponseEntity<ResponseDto<List<WorkflowDto>>> workflowList(
            @Schema(description = "Optional, filtered by version = latest or all")
            @RequestParam Optional<String> version,
            @Schema(description = "Optional, filtered by status = active or suspend")
            @RequestParam Optional<String> status) {
        return workflowService.workflowList(version, status);
    }

    @Operation(description = "suspend workflow by key or id, please choose one")
    @PostMapping(value = "/deployments/suspends")
    public void suspendProcessDefinition(@RequestBody RequestDTO requestDTO){
        workflowService.suspendProcessDefinition(requestDTO);
    }

    @Operation(description = "activate workflow by key or id, please choose one")
    @PostMapping(value = "/deployments/activates")
    public void activatesProcessDefinition(@RequestBody RequestDTO requestDTO){
        workflowService.activatesProcessDefinition(requestDTO);
    }

    @Operation(description = "delete specific workflow version")
    @DeleteMapping(value = "/deployments/{workflowId}")
    public void deleteWorkflowVersioning(
            @Schema(description = "Workflow ID = workflow version id")
            @PathVariable String workflowId) {
        workflowService.deleteWorkflowVersioning(workflowId);
    }

    @Operation(description = "Upload file to create a new deployment")
    @PostMapping(value = "/deployments", consumes = MediaType.MULTIPART_FORM_DATA)
    public void deployBPMN(
            @Schema(description = "Use Content-Type header with value of multipart/form-data with form-data body and Key = document, Value = uploaded file",type = "string",format = "binary")
            @RequestPart MultipartFile document){
        workflowService.createDeploymentProcess(document);
    }

    @Operation(description = "user to start the workflow")
    @PostMapping(value = "/{workflowName}/users/{userId}")
    public ResponseEntity<?> startWorkflow(
            @PathVariable String workflowName,
            @PathVariable String userId) {
        return workflowService.startProcessInstanceByKey(workflowName, userId);
    }

    @Operation(description = "get all deployment list")
    @GetMapping(value = "/deployments")
    public ResponseEntity<ResponseDto<List<DeploymentDto>>> deploymentList() {
        return workflowService.deploymentList();
    }
}
