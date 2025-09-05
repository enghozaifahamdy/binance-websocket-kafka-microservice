package com.example.Ingestion.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BaseEvent {
    String eventType;
    Long eventTimestamp;
    String symbol;
}
