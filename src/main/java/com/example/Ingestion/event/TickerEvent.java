package com.example.Ingestion.event;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class TickerEvent extends BaseEvent {
    private String priceChange; // Price change
    private String priceChangePercent; // Price change percent
    private String weightedAveragePrice; // Weighted average price
    private String firstTradeBeforeWindow; // First trade(F)-1 price (first trade before the 24hr rolling window)
    private String lastPrice; // Last price
    private String lastQuantity; // Last quantity
    private String bestBidPrice; // Best bid price
    private String bestBidQuantity; // Best bid quantity
    private String bestAskPrice; // Best ask price
    private String bestAskQuantity; // Best ask quantity
    private String openPrice; // Open price
    private String highPrice; // High price
    private String lowPrice; // Low price
    private String totalTradedBaseAssetVolume; // Total traded base asset volume
    private String totalTradedQuoteAssetVolume; // Total traded quote asset volume
    private Long statisticsOpenTime; // Statistics open time (milliseconds)
    private Long statisticsCloseTime; // Statistics close time (milliseconds)
    private Long firstTradeId; // First trade ID
    private Long lastTradeId; // Last trade ID
    private Long totalTradeCount; // Total number of trades
}
