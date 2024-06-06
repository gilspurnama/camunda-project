package org.camunda.bpm.engine.impl.variable.listener;

import org.camunda.bpm.engine.delegate.DelegateCaseVariableInstance;

public interface CaseVariableListenerInvocationUtil {

    static DelegateCaseVariableInstance getDelegation(CaseVariableListenerInvocation invocation){
        return invocation.variableInstance;
    }
}
