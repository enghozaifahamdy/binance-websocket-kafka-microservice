package com.example.Ingestion.mapper;


import com.example.Ingestion.event.TickerEvent;
import com.example.Ingestion.event.TradeEvent;
import com.fasterxml.jackson.databind.JsonNode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventMapper {

//    default TickerEvent tickerToKafkaEvent(JsonNode json){
//        TickerEvent tickerEvent = new TickerEvent();
//        tickerEvent.setEventType(getStringValue(json, "e"));
//        tickerEvent.setEventTimestamp(getLongValue(json, "E"));
//        tickerEvent.setSymbol(getStringValue(json, "s"));
//        return tickerEvent;
//    }
//
//    default TradeEvent tradeToKafkaEvent(JsonNode json){
//        TradeEvent tradeEvent = new TradeEvent();
//        tradeEvent.setEventType(getStringValue(json, "e"));
//        tradeEvent.setEventTimestamp(getLongValue(json, "E"));
//        tradeEvent.setSymbol(getStringValue(json, "s"));
//        return tradeEvent;
//    }
    @Mapping(target = "eventType", expression = "java(getStringValue(json, \"e\"))")
    @Mapping(target = "eventTimestamp", expression = "java(getLongValue(json, \"E\"))")
    @Mapping(target = "symbol", expression = "java(getStringValue(json, \"s\"))")
    @Mapping(target = "priceChange", expression = "java(getStringValue(json, \"p\"))")
    @Mapping(target = "priceChangePercent", expression = "java(getStringValue(json, \"P\"))")
    @Mapping(target = "weightedAveragePrice", expression = "java(getStringValue(json, \"w\"))")
    @Mapping(target = "firstTradeBeforeWindow", expression = "java(getStringValue(json, \"x\"))")
    @Mapping(target = "lastPrice", expression = "java(getStringValue(json, \"c\"))")
    @Mapping(target = "lastQuantity", expression = "java(getStringValue(json, \"Q\"))")
    @Mapping(target = "bestBidPrice", expression = "java(getStringValue(json, \"b\"))")
    @Mapping(target = "bestBidQuantity", expression = "java(getStringValue(json, \"B\"))")
    @Mapping(target = "bestAskPrice", expression = "java(getStringValue(json, \"a\"))")
    @Mapping(target = "bestAskQuantity", expression = "java(getStringValue(json, \"A\"))")
    @Mapping(target = "openPrice", expression = "java(getStringValue(json, \"o\"))")
    @Mapping(target = "highPrice", expression = "java(getStringValue(json, \"h\"))")
    @Mapping(target = "lowPrice", expression = "java(getStringValue(json, \"l\"))")
    @Mapping(target = "totalTradedBaseAssetVolume", expression = "java(getStringValue(json, \"v\"))")
    @Mapping(target = "totalTradedQuoteAssetVolume", expression = "java(getStringValue(json, \"q\"))")
    @Mapping(target = "statisticsOpenTime", expression = "java(getLongValue(json, \"O\"))")
    @Mapping(target = "statisticsCloseTime", expression = "java(getLongValue(json, \"C\"))")
    @Mapping(target = "firstTradeId", expression = "java(getLongValue(json, \"F\"))")
    @Mapping(target = "lastTradeId", expression = "java(getLongValue(json, \"L\"))")
    @Mapping(target = "totalTradeCount", expression = "java(getLongValue(json, \"n\"))")
    TickerEvent tickerToKafkaEvent(JsonNode json);

    @Mapping(target = "eventType", expression = "java(getStringValue(json, \"e\"))")
    @Mapping(target = "eventTimestamp", expression = "java(getLongValue(json, \"E\"))")
    @Mapping(target = "symbol", expression = "java(getStringValue(json, \"s\"))")
    @Mapping(target = "tradeId", expression = "java(getLongValue(json, \"t\"))")
    @Mapping(target = "price", expression = "java(getStringValue(json, \"p\"))")
    @Mapping(target = "quantity", expression = "java(getStringValue(json, \"q\"))")
    @Mapping(target = "tradeTime", expression = "java(getLongValue(json, \"T\"))")
    @Mapping(target = "isBuyerMarketMaker", expression = "java(getBooleanValue(json, \"m\"))")
    @Mapping(target = "ignore", expression = "java(getBooleanValue(json, \"M\"))")
    TradeEvent tradeToKafkaEvent(JsonNode json);

    default String getStringValue(JsonNode json, String field) {
        return json.has(field) && !json.get(field).isNull() ? json.get(field).asText() : null;
    }

    default Long getLongValue(JsonNode json, String field) {
        return json.has(field) && !json.get(field).isNull() ? json.get(field).asLong() : null;
    }

    default Boolean getBooleanValue(JsonNode json, String field) {
        return json.has(field) && !json.get(field).isNull() ? json.get(field).asBoolean() : null;
    }



}
