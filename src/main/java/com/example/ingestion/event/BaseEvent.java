package com.example.ingestion.event;

import lombok.Data;

@Data
public class BaseEvent {
    String eventType;
    Long eventTimestamp;
    String symbol;
}
