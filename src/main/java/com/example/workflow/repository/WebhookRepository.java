package com.example.workflow.repository;

import com.example.workflow.model.Webhook;
import com.example.workflow.model.view.WebhookView;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WebhookRepository extends JpaRepository<Webhook,String> {

    @Query("select w from Webhook w where lower(w.name) like ?1")
    List<Webhook> findAll(String name, Pageable pageable);

    @Query("select w.id as id,w.contentChecksum as contentChecksum from Webhook w where (w.tenantId is null or w.tenantId = ?1) and (w.processDefinitionId is null or w.processDefinitionId = ?2)")
    List<WebhookView> findBySelectedQuery(String tenantId, String processDefinitionId);

}
