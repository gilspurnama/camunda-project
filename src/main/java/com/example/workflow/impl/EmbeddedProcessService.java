package com.example.workflow.impl;

import com.example.workflow.dto.ProcessInstanceDto;
import com.example.workflow.dto.ProcessInstanceVariableDto;
import com.example.workflow.dto.ResponseDto;
import com.example.workflow.exception.ResponseException;
import com.example.workflow.service.ProcessService;
import lombok.AllArgsConstructor;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaInputOutput;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaOutputParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.workflow.util.ExceptionEnum.*;

@Service
@AllArgsConstructor
public class EmbeddedProcessService implements ProcessService {

    private final HistoryService historyService;

    private final RepositoryService repositoryService;

    @Override
    public ResponseEntity<ResponseDto<List<ProcessInstanceDto.Response>>> list(Integer page, Integer size) {
        if (page < 0) {
            throw new ResponseException(PAGE_START.message(), PAGE_START.httpStatus(), PAGE_START.code());
        }

        if (size < 1 || size > 10000) {
            throw new ResponseException(MINIMUM_MAXIMUM_SIZE.message(), MINIMUM_MAXIMUM_SIZE.httpStatus(), MINIMUM_MAXIMUM_SIZE.code());
        }

        List<HistoricProcessInstance> historicProcessInstances = historyService
                .createHistoricProcessInstanceQuery()
                .listPage(page, size);
        if (CollectionUtils.isEmpty(historicProcessInstances)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(new ResponseDto<>(
                historicProcessInstances.stream()
                        .map(processInstance ->
                                ProcessInstanceDto
                                        .Response
                                        .builder()
                                        .id(processInstance.getId())
                                        .businessKey(processInstance.getBusinessKey())
                                        .processDefinitionKey(processInstance.getProcessDefinitionKey())
                                        .processDefinitionName(processInstance.getProcessDefinitionName())
                                        .status(processInstance.getState())
                                        .build()
                        ).collect(Collectors.toList()))
        );
    }

    @Override
    public ResponseEntity<ResponseDto<ProcessInstanceDto.Response>> get(String id) {
        List<HistoricProcessInstance> historicProcessInstances = historyService
                .createHistoricProcessInstanceQuery()
                .processInstanceId(id)
                .list();

        if (CollectionUtils.isEmpty(historicProcessInstances)) {
            throw new ResponseException(PROCESS_NOT_FOUND.message(), PROCESS_NOT_FOUND.httpStatus(), PROCESS_NOT_FOUND.code());
        }

        HistoricProcessInstance processInstance = historicProcessInstances.get(0);
        return ResponseEntity.ok(new ResponseDto<>(
                ProcessInstanceDto
                        .Response
                        .builder()
                        .id(processInstance.getId())
                        .processDefinitionKey(processInstance.getProcessDefinitionKey())
                        .processDefinitionName(processInstance.getProcessDefinitionName())
                        .startTime(processInstance.getStartTime())
                        .endTime(processInstance.getEndTime())
                        .userId(processInstance.getStartUserId())
                        .status(processInstance.getState())
                        .build()
        ));
    }

    @Override
    public ResponseEntity<ResponseDto<List<ProcessInstanceVariableDto.Response>>> getOutputs(String id) {

        List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(id)
                .list();
        if (CollectionUtils.isEmpty(historicProcessInstances)) {
            throw new ResponseException(PROCESS_NOT_FOUND.message(), PROCESS_NOT_FOUND.httpStatus(), PROCESS_NOT_FOUND.code());
        }

        HistoricProcessInstance historicProcessInstance = historicProcessInstances.get(0);

        BpmnModelInstance modelInstance = repositoryService.getBpmnModelInstance(historicProcessInstance.getProcessDefinitionId());
        Collection<CamundaInputOutput> camundaInputOutputs = modelInstance.getModelElementsByType(CamundaInputOutput.class);

        Set<String> variableNames = camundaInputOutputs
                .stream()
                .flatMap(camundaInputOutput ->
                        camundaInputOutput.getCamundaOutputParameters().stream())
                .map(CamundaOutputParameter::getCamundaName)
                .collect(Collectors.toSet());
        List<ProcessInstanceVariableDto.Response> outputs = null;

        if (!variableNames.isEmpty()) {
            Map<String, HistoricActivityInstance> activityInstanceMap = historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(historicProcessInstance.getId())
                    .list()
                    .stream()
                    .collect(Collectors.toMap(HistoricActivityInstance::getExecutionId, Function.identity()));

            String[] outputVariableNames = new String[variableNames.size()];
            variableNames.toArray(outputVariableNames);

            List<HistoricVariableInstance> historicVariableInstances = historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(historicProcessInstance.getId())
                    .variableNameIn(outputVariableNames)
                    .list();

            if (!CollectionUtils.isEmpty(historicProcessInstances)) {
                outputs = historicVariableInstances
                        .stream()
                        .collect(Collectors.groupingBy(HistoricVariableInstance::getActivityInstanceId,
                                Collectors.mapping(historicVariableInstance ->
                                        ProcessInstanceVariableDto.Variable.builder().name(historicVariableInstance.getName())
                                                .value(historicVariableInstance.getValue())
                                                .build(), Collectors.toList()))
                        )
                        .entrySet()
                        .stream()
                        .map(stateVariables -> {
                                    HistoricActivityInstance historicActivityInstance = activityInstanceMap.get(stateVariables.getKey());
                                    return ProcessInstanceVariableDto.Response.builder()
                                            .executionId(historicActivityInstance.getExecutionId())
                                            .activityId(historicActivityInstance.getActivityId())
                                            .activityName(historicActivityInstance.getActivityName())
                                            .variables(stateVariables.getValue())
                                            .build();
                                }
                        )
                        .collect(Collectors.toList());
            }
        }

        return ResponseEntity.ok(new ResponseDto<>(outputs));
    }


}
