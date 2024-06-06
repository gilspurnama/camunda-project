package org.camunda.bpm.engine.impl.task.delegate;

import org.camunda.bpm.engine.delegate.DelegateTask;

public interface TaskListenerInvocationUtil {

    static DelegateTask getDelegateTask(TaskListenerInvocation invocation){
        return invocation.delegateTask;
    }

}
