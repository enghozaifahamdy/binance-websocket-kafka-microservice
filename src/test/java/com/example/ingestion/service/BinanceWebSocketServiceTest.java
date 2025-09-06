package com.example.ingestion.service;

import com.example.ingestion.enums.EventTypeEnum;
import com.example.ingestion.event.TickerEvent;
import com.example.ingestion.event.TradeEvent;
import com.example.ingestion.mapper.EventMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class BinanceWebSocketServiceTest {
    private BinanceWebSocketService binanceService;
    private KafkaEventPublisherService publisher;
    private EventMapper eventMapper;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        publisher = mock(KafkaEventPublisherService.class);
        eventMapper = mock(EventMapper.class);
        binanceService = new BinanceWebSocketService();

        // Inject mocks
        binanceService = spy(binanceService);
        binanceService = new BinanceWebSocketService();
        binanceService.getClass(); // avoid unused warnings
        // Reflection is used to inject mocks
        try {
            var publisherField = BinanceWebSocketService.class.getDeclaredField("eventPublisher");
            publisherField.setAccessible(true);
            publisherField.set(binanceService, publisher);

            var mapperField = BinanceWebSocketService.class.getDeclaredField("eventMapper");
            mapperField.setAccessible(true);
            mapperField.set(binanceService, eventMapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        objectMapper = new ObjectMapper();
    }

    @Test
    void testProcessTradeMessage() throws Exception {
        String json = "{ \"e\":\"trade\", \"s\":\"BTCUSDT\", \"t\":12345, \"p\":\"100.5\", \"q\":\"0.01\", \"T\":1670000000000, \"m\":true, \"M\":false }";
        TradeEvent tradeEvent = new TradeEvent();
        tradeEvent.setSymbol("BTCUSDT");
        tradeEvent.setPrice("100.5");
        when(eventMapper.tradeToKafkaEvent(any())).thenReturn(tradeEvent);

        // Call the private processIncomingMessage via reflection
        var method = BinanceWebSocketService.class.getDeclaredMethod("processIncomingMessage", String.class);
        method.setAccessible(true);
        method.invoke(binanceService, json);

        verify(eventMapper, times(1)).tradeToKafkaEvent(any());
        verify(publisher, times(1)).publishEvent(eq(EventTypeEnum.TRADE), eq(tradeEvent));
    }

    @Test
    void testProcessTickerMessage() throws Exception {
        String json = "{ \"e\":\"24hrTicker\", \"s\":\"ETHUSDT\", \"c\":\"2500.50\", \"P\":\"1.25\" }";
        TickerEvent tickerEvent = new TickerEvent();
        tickerEvent.setSymbol("ETHUSDT");
        tickerEvent.setLastPrice("2500.50");
        tickerEvent.setPriceChangePercent("1.25");
        when(eventMapper.tickerToKafkaEvent(any())).thenReturn(tickerEvent);

        var method = BinanceWebSocketService.class.getDeclaredMethod("processIncomingMessage", String.class);
        method.setAccessible(true);
        method.invoke(binanceService, json);

        verify(eventMapper, times(1)).tickerToKafkaEvent(any());
        verify(publisher, times(1)).publishEvent(eq(EventTypeEnum.TICKER), eq(tickerEvent));
    }

    @Test
    void testProcessUnknownMessage() throws Exception {
        String json = "{ \"e\":\"otherEvent\", \"s\":\"XRPUSDT\" }";

        var method = BinanceWebSocketService.class.getDeclaredMethod("processIncomingMessage", String.class);
        method.setAccessible(true);
        method.invoke(binanceService, json);

        verify(publisher, times(1)).publishEvent(eq(EventTypeEnum.UNKNOWN), any());
    }
}
