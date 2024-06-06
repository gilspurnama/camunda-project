package com.example.workflow.impl.business;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class PreMailerExecutorService implements ExecutionListener {

    @Override
    public void notify(DelegateExecution execution) {
        String subject = (String) execution.getVariable("subject");
        execution.setVariable("subject",subject+" [Testing Only]");
    }

}
