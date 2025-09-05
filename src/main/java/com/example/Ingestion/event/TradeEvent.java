package com.example.Ingestion.event;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class TradeEvent extends BaseEvent{
    private Long tradeId; // Trade ID
    private String price; // Price
    private String quantity; // Quantity
    private Long tradeTime; // Trade time (milliseconds)
    private Boolean isBuyerMarketMaker; // Is the buyer the market maker?
    private Boolean ignore; // Ignore field (always true for trade events)
}
