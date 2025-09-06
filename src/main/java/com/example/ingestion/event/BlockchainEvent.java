package com.example.ingestion.event;

import com.example.ingestion.enums.EventTypeEnum;
import lombok.Data;

@Data
public class BlockchainEvent {
    public BlockchainEvent(EventTypeEnum eventType, String source, Object payload) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.eventType = eventType.toString();
        this.source = source;
        this.payload = payload;
    }
    private String eventId;
    private String eventType;
    private String source;
    private Object payload;
}
