package com.example.ingestion.event;

import lombok.Data;

@Data
public class TradeEvent extends BaseEvent{
    private Long tradeId;
    private String price;
    private String quantity;
    private Long tradeTime;
    private Boolean isBuyerMarketMaker;
    private Boolean ignore;
}
