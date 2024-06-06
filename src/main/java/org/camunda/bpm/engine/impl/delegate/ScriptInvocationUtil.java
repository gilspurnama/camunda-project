package org.camunda.bpm.engine.impl.delegate;

import org.camunda.bpm.engine.delegate.VariableScope;

public interface ScriptInvocationUtil {

    static VariableScope getVariableScope(ScriptInvocation scriptInvocation){
        return scriptInvocation.scope;
    }
}
