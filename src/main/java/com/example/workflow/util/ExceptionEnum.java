package com.example.workflow.util;

import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

public enum ExceptionEnum {
    SUCCESS                                                 (200, HttpStatus.OK, null),
    USER_NOT_FOUND                                          (40001, HttpStatus.NOT_FOUND, "User Not Found"),
    USER_NOT_ASSIGN_TO_ROLE                                 (40002, HttpStatus.NOT_FOUND, "User Not Assign to Any Role"),
    INSERT_KEY_ID                                          (50001, HttpStatus.BAD_REQUEST, "Please Insert Key or Id"),
    PAGE_START                                          (50002, HttpStatus.BAD_REQUEST, "Page Start From 0"),
    MINIMUM_MAXIMUM_SIZE                                          (50003, HttpStatus.BAD_REQUEST, "Minimum Size is 1 and Max Size is 10000"),
    UNAUTHORIZED                                         (50004, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED"),
    UNKNOWN_ERROR                                       (50999, null, null),
    WORKFLOW_NOT_FOUND                                 (51001, HttpStatus.NOT_FOUND, "Workflow Not Found"),
    WORKFLOW_WRONG_UPLOAD_FILE                                      (51002, HttpStatus.BAD_REQUEST, "Please Upload the Correct File"),
    PROCESS_NOT_FOUND                                          (52001, HttpStatus.NOT_FOUND, "Process Not Found"),
    WEBHOOK_NOT_FOUND                                      (53001, HttpStatus.NOT_FOUND, "Webhook Not Found"),
    TASK_NOT_FOUND                                     (54001, HttpStatus.NOT_FOUND, "Task Not Found"),
    TASK_NOT_ASSIGNED                                 (54002, HttpStatus.BAD_REQUEST, "Not Assigned Task, Please Assign An User to The Task"),
    TASK_SUSPENDED                                    (54003, HttpStatus.BAD_REQUEST, "Task Is Suspended"),
    USER_ID_REQUIRED                                (54004, HttpStatus.BAD_REQUEST, "UserId is Required"),
    USER_ID_NOT_FOUND                               (54005, HttpStatus.NOT_FOUND, "UserId Does Not Exists");

    private final Integer code;
    private final HttpStatus httpStatus;
    private final String message;

    ExceptionEnum(Integer code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public Integer code() {
        return this.code;
    }

    public HttpStatus httpStatus() {
        return this.httpStatus;
    }

    public String message() {
        return this.message;
    }

    public static HttpStatus getHttpStatus(Integer code){
        for (ExceptionEnum item : ExceptionEnum.values()) {
            if (code.equals(item.code())) {
                return item.httpStatus;
            }
        }
        return HttpStatus.BAD_REQUEST;
    }

    public static String getMessage(Integer code, String defaultMessage) {
        for (ExceptionEnum item : ExceptionEnum.values()) {
            if (code.equals(item.code()) && !StringUtils.hasLength(item.message)) {
                return item.message;
            }
        }
        return defaultMessage;
    }

    public static String getMessage(ExceptionEnum exceptionEnum) {
        return getMessage(exceptionEnum, exceptionEnum.message());
    }

    public static String getMessage(ExceptionEnum exceptionEnum, String customErrorMessage) {
        return String.format("%d - %s", exceptionEnum.code(), customErrorMessage);
    }
}
