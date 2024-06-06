package com.example.workflow.impl;

import com.example.workflow.dto.ResponseDto;
import com.example.workflow.dto.WebhookDto;
import com.example.workflow.dto.cache.Hook;
import com.example.workflow.exception.ResponseException;
import com.example.workflow.model.Webhook;
import com.example.workflow.model.view.WebhookView;
import com.example.workflow.repository.WebhookRepository;
import com.example.workflow.service.WebhookService;
import com.example.workflow.util.CacheManager;
import com.example.workflow.util.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import lombok.AllArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestTemplate;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.workflow.util.ExceptionEnum.*;
import static com.example.workflow.util.StringUtils.bie;

@Service
@AllArgsConstructor
public class BasicWebhookService implements WebhookService {

    private final WebhookRepository webhookRepository;

    private final ObjectMapper objectMapper;

    private final RestTemplate restTemplate;

    private final CacheManager.Cache<String, Hook> mustacheCache;

    @Override
    public ResponseEntity<ResponseDto<WebhookDto.Response>> create(WebhookDto.Request request) throws Exception{

        Webhook webhook = new Webhook();
        webhook.setName(request.getName());
        webhook.setContent(request.getContent());
        String checksum = bie(request.getContent())+ bie(request.getUrl())+bie(request.getMethod());
        webhook.setUrl(request.getUrl());
        if(!CollectionUtils.isEmpty(request.getHeader())) {
            String headerSerialize =objectMapper.writeValueAsString(request.getHeader());
            webhook.setHeader(headerSerialize);
            checksum+=headerSerialize;
        }

        webhook.setContentChecksum(DigestUtils.md5DigestAsHex(checksum.getBytes()));
        webhook.setTenantId(request.getTenantId());

        webhook.setProcessDefinitionId(request.getProcessDefinitionId());
        webhook.setMethod(request.getMethod());
        webhook = webhookRepository.save(webhook);

        WebhookDto.Response response = WebhookDto.Response
                .builder()
                .id(webhook.getId())
                .name(webhook.getName())
                .content(webhook.getContent())
                .url(webhook.getUrl())
                .header(request.getHeader())
                .tenantId(webhook.getTenantId())
                .processDefinitionId(webhook.getProcessDefinitionId())
                .build();
        return ResponseEntity.ok(new ResponseDto<>(response));
    }

    @Override
    public ResponseEntity<ResponseDto<WebhookDto.Response>> delete(String id) throws Exception {
        Webhook webhook = webhookRepository.findById(id).orElseThrow(() -> new ResponseException(WEBHOOK_NOT_FOUND.message(), WEBHOOK_NOT_FOUND.httpStatus(), WEBHOOK_NOT_FOUND.code()));
        webhookRepository.delete(webhook);
        WebhookDto.Response response = WebhookDto.Response
                .builder()
                .id(webhook.getId())
                .name(webhook.getName())
                .content(webhook.getContent())
                .method(webhook.getMethod())
                .url(webhook.getUrl())
                .header(!StringUtils.isEmpty(webhook.getHeader())?objectMapper.readValue(webhook.getHeader(), new TypeReference<Map<String,String>>() {
                }): Collections.emptyMap())
                .tenantId(webhook.getTenantId())
                .processDefinitionId(webhook.getProcessDefinitionId())
                .build();
        return ResponseEntity.ok(new ResponseDto<>(response));
    }

    @Override
    public ResponseEntity<ResponseDto<WebhookDto.Response>> update(String id, WebhookDto.Request request) throws Exception{
        Webhook webhook = webhookRepository.findById(id).orElseThrow(()->new ResponseException(WEBHOOK_NOT_FOUND.message(), WEBHOOK_NOT_FOUND.httpStatus(), WEBHOOK_NOT_FOUND.code()));
        webhook.setName(request.getName());
        webhook.setContent(request.getContent());
        String checksum = bie(request.getContent())+ bie(request.getUrl())+bie(request.getMethod());
        webhook.setUrl(request.getUrl());
        if(!CollectionUtils.isEmpty(request.getHeader())) {
            String headerSerialize =objectMapper.writeValueAsString(request.getHeader());
            webhook.setHeader(headerSerialize);
            checksum+=headerSerialize;
        }

        webhook.setContentChecksum(DigestUtils.md5DigestAsHex(checksum.getBytes()));
        webhook.setTenantId(request.getTenantId());
        webhook.setProcessDefinitionId(request.getProcessDefinitionId());
        webhook.setMethod(request.getMethod());
        webhook = webhookRepository.save(webhook);
        WebhookDto.Response response = WebhookDto.Response
                .builder()
                .id(webhook.getId())
                .name(webhook.getName())
                .content(webhook.getContent())
                .url(webhook.getUrl())
                .method(webhook.getMethod())
                .header(request.getHeader())
                .tenantId(webhook.getTenantId())
                .processDefinitionId(webhook.getProcessDefinitionId())
                .build();
        return ResponseEntity.ok(new ResponseDto<>(response));
    }

    @Override
    public ResponseEntity<ResponseDto<List<WebhookDto.Response>>> get(String name,Integer page,Integer size) {
        if(StringUtils.isEmpty(name)){
            name = "";
        }
        name = "%"+name.toLowerCase()+"%";

        if (page < 0) {
            throw new ResponseException(PAGE_START.message(), PAGE_START.httpStatus(), PAGE_START.code());
        }

        if (size < 1 || size > 10000) {
            throw new ResponseException(MINIMUM_MAXIMUM_SIZE.message(), MINIMUM_MAXIMUM_SIZE.httpStatus(), MINIMUM_MAXIMUM_SIZE.code());
        }

        List<Webhook> datum = webhookRepository.findAll(name, PageRequest.of(page,size));
        if(CollectionUtils.isEmpty(datum)){
            return ResponseEntity.noContent().build();
        }

        List<WebhookDto.Response> responses = datum.stream().map(data -> WebhookDto.Response.builder()
                        .id(data.getId())
                        .name(data.getName())
                        .content(data.getContent())
                        .url(data.getUrl())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ResponseDto<>(responses));
    }

    @Override
    public ResponseEntity<ResponseDto<WebhookDto.Response>> get(String id) throws Exception{
        Webhook webhook = webhookRepository.findById(id).orElseThrow(()->new ResponseException(WEBHOOK_NOT_FOUND.message(), WEBHOOK_NOT_FOUND.httpStatus(), WEBHOOK_NOT_FOUND.code()));
        WebhookDto.Response response = WebhookDto.Response
                .builder()
                .id(webhook.getId())
                .name(webhook.getName())
                .content(webhook.getContent())
                .method(webhook.getMethod())
                .url(webhook.getUrl())
                .header(!StringUtils.isEmpty(webhook.getHeader())?objectMapper.readValue(webhook.getHeader(), new TypeReference<Map<String,String>>() {
                }): Collections.emptyMap())
                .tenantId(webhook.getTenantId())
                .processDefinitionId(webhook.getProcessDefinitionId())
                .build();
        return ResponseEntity.ok(new ResponseDto<>(response));
    }


    private void publishEvent(DelegateExecution executionEvent) throws Exception{
        if(StringUtils.isEmpty(executionEvent.getActivityInstanceId()) || !"start".equalsIgnoreCase(executionEvent.getEventName())){
            return;
        }

        List<WebhookView> webhooks = webhookRepository
                .findBySelectedQuery(executionEvent.getTenantId(),executionEvent.getProcessDefinitionId());

        Map<String,String> variables = new HashMap<>();
        variables.put("activityId",executionEvent.getCurrentActivityId());
        variables.put("activityName",executionEvent.getCurrentActivityName());
        variables.put("businessKey",executionEvent.getProcessBusinessKey());
        variables.put("processDefinitionId",executionEvent.getProcessDefinitionId());

        for(WebhookView webhook:webhooks) {
            Hook hook = mustacheCache.get(webhook.getId(),existing->existing.getHash().equals(webhook.getContentChecksum()),(id)->{
                try {
                    Webhook source = webhookRepository.findById(id).get();
                    HttpHeaders headers = new HttpHeaders();
                    if (!StringUtils.isEmpty(source.getHeader())) {
                        Map<String, String> rawHeaders = objectMapper.readValue(source.getHeader(), new TypeReference<Map<String,String>>() {
                        });
                        rawHeaders.forEach(headers::set);
                    }
                    return Hook.builder()
                            .hash(webhook.getContentChecksum())
                            .url(source.getUrl())
                            .header(headers)
                            .method(HttpMethod.valueOf(source.getMethod()))
                            .mustache(compileMustache(source.getContent()))
                            .build();
                }
                catch (Exception e){
                    throw new RuntimeException(e);
                }
            });
            StringWriter writer = new StringWriter();
            hook.getMustache().execute(writer,variables);
            RequestEntity<String> entity = new RequestEntity<>(writer.toString(), hook.getHeader(),hook.getMethod(),URI.create(hook.getUrl()));
            restTemplate.exchange(entity,String.class);
        }
    }

    private Mustache compileMustache(String content){
        MustacheFactory mustacheFactory = new DefaultMustacheFactory();
        return mustacheFactory.compile(new StringReader(content),"id");
    }



   @EventListener
    public void postExecution(DelegateExecution execution) throws Exception{
        publishEvent(execution);
    }

}
