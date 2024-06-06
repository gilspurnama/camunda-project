package com.example.workflow.impl;

import com.example.workflow.model.ErrorLog;
import com.example.workflow.repository.ErrorLogRepository;
import com.example.workflow.service.ErrorLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class BasicErrorLogService implements ErrorLogService {

    private final ErrorLogRepository errorLogRepository;

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void save(ErrorLog errorLog) {
        errorLogRepository.save(errorLog);
    }
}
