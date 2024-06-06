package com.example.workflow.handler;

import com.example.workflow.model.ErrorLog;
import com.example.workflow.service.ErrorLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.*;
import org.camunda.bpm.engine.impl.bpmn.delegate.ActivityBehaviorInvocation;
import org.camunda.bpm.engine.impl.bpmn.delegate.ActivityBehaviorSignalInvocation;
import org.camunda.bpm.engine.impl.bpmn.delegate.ExecutionListenerInvocation;
import org.camunda.bpm.engine.impl.bpmn.delegate.JavaDelegateInvocation;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmmn.delegate.CaseExecutionListenerInvocation;
import org.camunda.bpm.engine.impl.core.instance.CoreExecution;
import org.camunda.bpm.engine.impl.delegate.DelegateInvocation;
import org.camunda.bpm.engine.impl.delegate.ScriptInvocation;
import org.camunda.bpm.engine.impl.delegate.ScriptInvocationUtil;
import org.camunda.bpm.engine.impl.interceptor.DelegateInterceptor;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.task.delegate.TaskListenerInvocation;
import org.camunda.bpm.engine.impl.task.delegate.TaskListenerInvocationUtil;
import org.camunda.bpm.engine.impl.variable.listener.CaseVariableListenerInvocation;
import org.camunda.bpm.engine.impl.variable.listener.CaseVariableListenerInvocationUtil;
import org.camunda.bpm.engine.impl.variable.listener.DelegateCaseVariableInstanceImpl;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ErrorHandler implements DelegateInterceptor, InitializingBean {

    private DelegateInterceptor delegateInterceptor;

    private final ProcessEngine processEngine;

    private final ErrorLogService errorLogService;

    private void handle(Exception e,DelegateInvocation invocation) throws Exception{
        String definitionId = null;
        String instanceId = null;
        String activityId = null;
        String activityName = null;
        String taskId = null;
        String tenantId = null;
        String eventName = null;
        if(invocation instanceof ActivityBehaviorInvocation || invocation instanceof ActivityBehaviorSignalInvocation){
            ActivityExecution activityExecution = (ActivityExecution) invocation.getContextExecution();
            definitionId = activityExecution.getProcessDefinitionId();
            activityId = activityExecution.getCurrentActivityId();
            activityName = activityExecution.getCurrentActivityName();
            instanceId = activityExecution.getProcessInstanceId();
            tenantId = activityExecution.getTenantId();
            eventName = activityExecution.getEventName();
        }
        else if(invocation instanceof CaseExecutionListenerInvocation){
            DelegateCaseExecution delegateCaseExecution = (DelegateCaseExecution) invocation.getContextExecution();
            activityId = delegateCaseExecution.getActivityId();
            activityName = delegateCaseExecution.getActivityName();
            tenantId = delegateCaseExecution.getTenantId();
            eventName = delegateCaseExecution.getEventName();
            instanceId = delegateCaseExecution.getCaseInstanceId();
            definitionId = delegateCaseExecution.getCaseDefinitionId();
        }
        else if(invocation instanceof CaseVariableListenerInvocation){
            DelegateCaseExecution delegateCaseExecution = (DelegateCaseExecution) invocation.getContextExecution();
            DelegateCaseVariableInstanceImpl caseVariableInstance = (DelegateCaseVariableInstanceImpl) CaseVariableListenerInvocationUtil.getDelegation((CaseVariableListenerInvocation) delegateCaseExecution);
            if(delegateCaseExecution != null) {
                activityId = delegateCaseExecution.getActivityId();
                activityName = delegateCaseExecution.getActivityName();
                tenantId = delegateCaseExecution.getTenantId();
                eventName = delegateCaseExecution.getEventName();
                instanceId = delegateCaseExecution.getCaseInstanceId();
                definitionId = delegateCaseExecution.getCaseDefinitionId();
            }

            if(caseVariableInstance != null){
                taskId = caseVariableInstance.getTaskId();
            }
        }
        else if(invocation instanceof ExecutionListenerInvocation || invocation instanceof JavaDelegateInvocation || invocation instanceof  ScriptInvocation){
            DelegateExecution execution = (DelegateExecution) invocation.getContextExecution();
            if(execution != null) {
                activityId = execution.getCurrentActivityId();
                activityName = execution.getCurrentActivityName();
                tenantId = execution.getTenantId();
                eventName = execution.getEventName();
                instanceId = execution.getProcessInstanceId();
                definitionId = execution.getProcessDefinitionId();
            }
        }
        else if(invocation instanceof ScriptInvocation){
            DelegateExecution execution = (DelegateExecution) invocation.getContextExecution();
            VariableScope variableScope = ScriptInvocationUtil.getVariableScope((ScriptInvocation) invocation);
            if(execution != null) {
                activityId = execution.getCurrentActivityId();
                activityName = execution.getCurrentActivityName();
                tenantId = execution.getTenantId();
                eventName = execution.getEventName();
                instanceId = execution.getProcessInstanceId();
                definitionId = execution.getProcessDefinitionId();
            }
            else if (variableScope instanceof DelegateExecution) {
                execution  = ((DelegateExecution) variableScope);
                activityId = execution.getCurrentActivityId();
                activityName = execution.getCurrentActivityName();
                tenantId = execution.getTenantId();
                eventName = execution.getEventName();
                instanceId = execution.getProcessInstanceId();
                definitionId = execution.getProcessDefinitionId();
            } else if (variableScope instanceof TaskEntity) {
                TaskEntity task = (TaskEntity) variableScope;
                activityId = task.getExecution().getCurrentActivityId();
                activityName = task.getExecution().getCurrentActivityName();
                tenantId = task.getTenantId();
                eventName = task.getEventName();
                instanceId = task.getProcessInstanceId();
                definitionId = task.getProcessDefinitionId();
                taskId = task.getId();
            } else if (variableScope instanceof DelegateCaseExecution) {
                DelegateCaseExecution delegateCaseExecution = (DelegateCaseExecution) variableScope;
                activityId = delegateCaseExecution.getActivityId();
                activityName = delegateCaseExecution.getActivityName();
                tenantId = delegateCaseExecution.getTenantId();
                eventName = delegateCaseExecution.getEventName();
                instanceId = delegateCaseExecution.getCaseInstanceId();
                definitionId = delegateCaseExecution.getCaseDefinitionId();
            }
        }
        else if(invocation instanceof TaskListenerInvocation){
            CoreExecution execution = (CoreExecution)invocation.getContextExecution();
            DelegateTask delegateTask = TaskListenerInvocationUtil.getDelegateTask((TaskListenerInvocation) invocation);
            if(execution != null) {
                tenantId = execution.getTenantId();
                eventName = execution.getEventName();
            }

            if(delegateTask != null){
                taskId = delegateTask.getId();
                activityId = delegateTask.getExecution().getCurrentActivityId();
                activityName = delegateTask.getExecution().getCurrentActivityName();
                instanceId = delegateTask.getProcessInstanceId();
                definitionId = delegateTask.getProcessDefinitionId();
            }
        }

        ErrorLog errorLog = new ErrorLog();
        errorLog.setActivityId(activityId);
        errorLog.setActivityName(activityName);
        errorLog.setDefinitionId(definitionId);
        errorLog.setInstanceId(instanceId);
        errorLog.setTenantId(tenantId);
        errorLog.setEvetName(eventName);
        errorLog.setTaskId(taskId);
        errorLog.setMessage(e.getMessage());
        errorLog.setType(e.getClass().getName());
        if(e instanceof BpmnError){
            BpmnError bpmnError = (BpmnError)e;
            errorLog.setErrorCode(bpmnError.getErrorCode());
            errorLog.setCode(bpmnError.getCode());
        }
        else if(e instanceof ProcessEngineException){
            ProcessEngineException processEngineException = (ProcessEngineException)e;
            errorLog.setMessage(processEngineException.getMessage());
            errorLog.setCode(processEngineException.getCode());
        }
        else{
            errorLog.setMessage(e.getMessage());
        }
        errorLogService.save(errorLog);
        log.error("errorHandler",e);
        throw e;
    }

    @Override
    public void handleInvocation(DelegateInvocation invocation) throws Exception {
        try {
            delegateInterceptor.handleInvocation(invocation);
        }
        catch (Exception t){
            handle(t,invocation);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ProcessEngineConfigurationImpl configuration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
        this.delegateInterceptor = configuration.getDelegateInterceptor();
        configuration.setDelegateInterceptor(this);
    }
}
