package com.example.workflow.controller;

import com.example.workflow.dto.ResponseDto;
import com.example.workflow.dto.WebhookDto;
import com.example.workflow.service.WebhookService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/webhook")
@OpenAPIDefinition(info = @Info(title = "API Collection of Webhook",description = "Webhook API is used to register url which will be called back by system to notify the changes of state of a process instance"))
public class WebhookController {

    private final WebhookService webhookService;

    /**
     *  sample of request payload for telegeram
     *
     *     {
     *       "name": "telegram",
     *       "url": "https://api.telegram.org/bot6953706218:AAFT9xCsRvmdmYSX9BfsP-2pT00LFVzOmDM/sendMessage",
     *       "content": "{\"chat_id\":\"-4182860336\",\"telegram_parse_mode\":\"Markdown\",\"text\":\"State Currently At {{activityName}}\"}"
     *     }
     */
    @PostMapping
    @Operation(description = "create a webhook")
    public ResponseEntity<ResponseDto<WebhookDto.Response>> create(@Valid @RequestBody WebhookDto.Request request) throws Exception{
        return webhookService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(description = "update a webhook")
    public ResponseEntity<ResponseDto<WebhookDto.Response>> update(@PathVariable("id") String id,@Valid @RequestBody WebhookDto.Request request) throws Exception{
        return webhookService.update(id,request);
    }

    @DeleteMapping("/{id}")
    @Operation(description = "delete a webhook")
    public ResponseEntity<ResponseDto<WebhookDto.Response>> delete(@PathVariable("id") String id) throws Exception{
        return webhookService.delete(id);
    }

    @GetMapping
    @Operation(description = "get list of webhooks")
    public ResponseEntity<ResponseDto<List<WebhookDto.Response>>> get(@RequestParam(required = false) String name
            ,@RequestParam Integer page
            ,@RequestParam Integer size) throws Exception{
        Authentication contextHolder = SecurityContextHolder.getContext().getAuthentication();
        System.out.println();
        return webhookService.get(name,page,size);
    }


    @GetMapping("/{id}")
    @Operation(description = "get a webhook")
    public ResponseEntity<ResponseDto<WebhookDto.Response>> get(@PathVariable("id") String id) throws Exception{
        return webhookService.get(id);
    }


}
