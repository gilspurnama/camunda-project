package com.example.workflow.impl.business;

import com.example.workflow.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import sendinblue.ApiClient;
import sibApi.TransactionalEmailsApi;
import sibModel.*;

@Component
@Slf4j
@AllArgsConstructor
public class MailerExecutorService implements JavaDelegate {

    private final ApiClient apiClient;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String toEmail = (String) execution.getVariable("toEmail");
        String toName = (String) execution.getVariable("toName");
        String content = (String) execution.getVariable("content");
        String subject = (String) execution.getVariable("subject");

        String toCc = (String) execution.getVariable("toCc");
        String toBcc = (String) execution.getVariable("toBcc");

        SendSmtpEmail emailPayload = new SendSmtpEmail();

        SendSmtpEmailTo emailToPayload = new SendSmtpEmailTo();
        emailToPayload.setEmail(toEmail);
        emailToPayload.setName(toName);

        emailPayload.addToItem(emailToPayload);

        emailPayload.subject(subject);

        emailPayload.setHtmlContent(content);

        SendSmtpEmailSender senderPayload = new SendSmtpEmailSender();
        senderPayload.setEmail("fahmih.rabbani@gmail.com");
        senderPayload.setName("noreply");
        emailPayload.sender(senderPayload);

        if(!StringUtils.isEmpty(toCc)){
            SendSmtpEmailCc cc = new SendSmtpEmailCc();
            cc.setEmail(toCc);
            emailPayload.addCcItem(cc);
        }

        if(!StringUtils.isEmpty(toBcc)){
            SendSmtpEmailBcc bcc = new SendSmtpEmailBcc();
            bcc.setEmail(toBcc);
            emailPayload.addBccItem(bcc);
        }

        TransactionalEmailsApi transactionalEmailsApi = new TransactionalEmailsApi(apiClient);
        transactionalEmailsApi.sendTransacEmail(emailPayload);
        log.info("send email succes to "+toEmail);
    }

}
