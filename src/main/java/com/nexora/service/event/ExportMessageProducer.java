package com.nexora.service.event;

import com.nexora.model.inventory.event.ExportRequestEvent;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ExportMessageProducer {

    private final AmqpTemplate amqpTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key.export}")
    private String exportRoutingKey;

    public ExportMessageProducer(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    public void sendExportRequest(ExportRequestEvent event) {
        amqpTemplate.convertAndSend(exchangeName, exportRoutingKey, event);
    }
}
