package com.example.ingestion.service;

import com.example.ingestion.enums.EventTypeEnum;
import com.example.ingestion.event.BlockchainEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
public class KafkaEventPublisherServiceTest {
    @Mock
    private KafkaTemplate<String, BlockchainEvent> kafkaTemplate;

    @InjectMocks
    private KafkaEventPublisherService publisher;

    // We'll set these private @Value fields by reflection before tests
    private static final String TRADES_TOPIC = "test-trades";
    private static final String TICKERS_TOPIC = "test-tickers";
    private static final String EVENTS_TOPIC = "test-events";

    @BeforeEach
    void setup() throws Exception {
        // Inject topic names into private fields via reflection
        setField(publisher, "tradesTopic", TRADES_TOPIC);
        setField(publisher, "tickersTopic", TICKERS_TOPIC);
        setField(publisher, "eventsTopic", EVENTS_TOPIC);
    }

    @Test
    void publishEvent_trade_shouldSendToTradesTopic() {
        // arrange
        // kafkaTemplate.send returns a CompletableFuture - mimic successful send
        CompletableFuture<SendResult<String, BlockchainEvent>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(eq(TRADES_TOPIC), any(BlockchainEvent.class))).thenReturn(future);

        // act
        publisher.publishEvent(EventTypeEnum.TRADE, new Object());

        // assert
        ArgumentCaptor<BlockchainEvent> captor = ArgumentCaptor.forClass(BlockchainEvent.class);
        verify(kafkaTemplate, times(1)).send(eq(TRADES_TOPIC), captor.capture());
        BlockchainEvent sent = captor.getValue();
        assertNotNull(sent);
        assertEquals(EventTypeEnum.TRADE.toString(), sent.getEventType());
    }

    @Test
    void publishEvent_ticker_shouldSendToTickersTopic() {
        // arrange
        CompletableFuture<SendResult<String, BlockchainEvent>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(eq(TICKERS_TOPIC), any(BlockchainEvent.class))).thenReturn(future);

        // act
        publisher.publishEvent(EventTypeEnum.TICKER, "payload");

        // assert
        ArgumentCaptor<BlockchainEvent> captor = ArgumentCaptor.forClass(BlockchainEvent.class);
        verify(kafkaTemplate, times(1)).send(eq(TICKERS_TOPIC), captor.capture());
        BlockchainEvent sent = captor.getValue();
        assertNotNull(sent);
        assertEquals(EventTypeEnum.TICKER.toString(), sent.getEventType());
        assertEquals("payload", sent.getPayload());
    }

    @Test
    void publishEvent_unknown_shouldSendToEventsTopic() {
        // arrange
        CompletableFuture<SendResult<String, BlockchainEvent>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(eq(EVENTS_TOPIC), any(BlockchainEvent.class))).thenReturn(future);

        // act
        publisher.publishEvent(EventTypeEnum.UNKNOWN, 12345);

        // assert
        ArgumentCaptor<BlockchainEvent> captor = ArgumentCaptor.forClass(BlockchainEvent.class);
        verify(kafkaTemplate, times(1)).send(eq(EVENTS_TOPIC), captor.capture());
        BlockchainEvent sent = captor.getValue();
        assertNotNull(sent);
        assertEquals(EventTypeEnum.UNKNOWN.toString(), sent.getEventType());
        assertEquals(12345, sent.getPayload());
    }

    @Test
    void publishEvent_whenKafkaThrows_shouldNotPropagate() {
        // arrange: kafkaTemplate.send throws runtime exception
        when(kafkaTemplate.send(anyString(), any(BlockchainEvent.class)))
                .thenThrow(new RuntimeException("kafka error"));

        // act & assert: method should not propagate exception
        assertDoesNotThrow(() -> publisher.publishEvent(EventTypeEnum.TRADE, "p"));
        // verify send was attempted
        verify(kafkaTemplate, times(1)).send(anyString(), any(BlockchainEvent.class));
    }

    // helper - sets private field via reflection
    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}
