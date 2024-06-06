package com.example.workflow.service;

import com.example.workflow.dto.ResponseDto;
import com.example.workflow.dto.WebhookDto;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface WebhookService {

    ResponseEntity<ResponseDto<WebhookDto.Response>> create(WebhookDto.Request request) throws Exception;

    ResponseEntity<ResponseDto<WebhookDto.Response>> delete(String id) throws Exception;

    ResponseEntity<ResponseDto<WebhookDto.Response>> update(String id, WebhookDto.Request request) throws Exception;

    ResponseEntity<ResponseDto<List<WebhookDto.Response>>> get(String name,Integer page,Integer size);

    ResponseEntity<ResponseDto<WebhookDto.Response>> get(String id) throws Exception;
}
