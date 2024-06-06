package com.example.workflow.configuration;

import com.example.workflow.dto.ResponseDto;
import com.example.workflow.exception.ResponseException;
import com.example.workflow.model.ErrorLog;
import com.example.workflow.repository.ErrorLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static java.time.ZoneOffset.UTC;

@Slf4j
@RestControllerAdvice
public class ExceptionMapperConfiguration {

    private final ErrorLogRepository errorLogRepository;


    public ExceptionMapperConfiguration(ErrorLogRepository errorLogRepository) {
        this.errorLogRepository = errorLogRepository;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto<?>> handler(Exception exception, HttpServletRequest httpServletRequest) throws IOException {
        log.error("ExceptionHandler",exception);
        if(exception instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException methodArgumentNotValidException = (MethodArgumentNotValidException) exception;
            return ResponseEntity.badRequest().body(new ResponseDto<>(null, methodArgumentNotValidException.getFieldError().getDefaultMessage()));
        }
        else if(exception instanceof ResponseException){
            ResponseException responseException = (ResponseException) exception;
            ResponseEntity.BodyBuilder bodyBuilder = ResponseEntity.status(responseException.getStatus());
            if(responseException.getMessage() == null){
                return bodyBuilder.build();
            }
            ErrorLog errorLog = new ErrorLog();Map<String, String> headers = Collections.list(httpServletRequest.getHeaderNames())
                    .stream()
                    .collect(Collectors.toMap(h -> h, httpServletRequest::getHeader));

            errorLog.setErrorCode(String.valueOf(responseException.getErrorCode()));
            errorLog.setMessage(responseException.getMessage());
            errorLog.setActivityName(String.valueOf(((ResponseException) exception).getStatus().series()));
            errorLog.setType(((ResponseException) exception).getStatus().name());
            errorLog.setRequestBody(httpServletRequest.getReader().lines().collect(Collectors.joining(System.lineSeparator())).replaceAll("\\s",""));
            errorLog.setRequestMethod(httpServletRequest.getMethod());
            errorLog.setRequestPath(httpServletRequest.getRequestURI());
            errorLog.setRequestHeader(headers.toString().replaceAll("\\s",""));
            errorLog.setCreatedAt(LocalDateTime.now(UTC));

            errorLogRepository.saveAndFlush(errorLog);
            return bodyBuilder.body(new ResponseDto<>(responseException.getMessage(), responseException.getErrorCode()));
        }
        else if(exception instanceof AccessDeniedException){
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDto<>(exception.getMessage()));
        }
        else{
            ResponseEntity.BodyBuilder bodyBuilder = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR);
            String errorCode = null;
            if(exception instanceof BpmnError){
                errorCode = ((BpmnError)exception).getErrorCode();
            }

            ResponseDto<?> responseDto = new ResponseDto<>(exception.getMessage());
            responseDto.errorCode = Integer.valueOf(errorCode);
            return bodyBuilder.body(responseDto);
        }
    }

}
