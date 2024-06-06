package com.example.workflow.model;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Data
@Entity
@Table(name = "webhook")
public class Webhook {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @Column(name = "name",nullable = false)
    private String name;

    @Column(name = "url",nullable = false)
    private String url;

    @Column(name = "tenant_id")
    private String tenantId;

    @Column(name = "content")
    private String content;

    @Column(name = "method")
    private String method;

    @Column(name = "header",columnDefinition = "text")
    private String header;

    @Column(name = "content_checksum")
    private String contentChecksum;

    @Column(name = "process_definition_id")
    private String processDefinitionId;
}
