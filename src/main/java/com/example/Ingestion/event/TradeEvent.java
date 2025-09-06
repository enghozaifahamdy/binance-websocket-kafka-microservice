package com.example.Ingestion.event;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class TradeEvent extends BaseEvent{
    private Long tradeId;
    private String price;
    private String quantity;
    private Long tradeTime;
    private Boolean isBuyerMarketMaker;
    private Boolean ignore;
}
