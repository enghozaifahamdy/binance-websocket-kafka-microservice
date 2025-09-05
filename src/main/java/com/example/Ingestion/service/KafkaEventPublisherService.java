package com.example.Ingestion.service;

import com.example.Ingestion.enums.EventTypeEnum;
import com.example.Ingestion.event.BlockchainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class KafkaEventPublisherService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaEventPublisherService.class);

    @Autowired
    private KafkaTemplate<String, BlockchainEvent> kafkaTemplate;

    @Value("${kafka.topics.blockchain-trades}")
    private String tradesTopic;

    @Value("${kafka.topics.blockchain-tickers}")
    private String tickersTopic;

    @Value("${kafka.topics.blockchain-events}")
    private String eventsTopic;

    private final AtomicLong publishedEvents = new AtomicLong(0);

    public void publishEvent(EventTypeEnum eventType, Object data) {
        switch (eventType) {
            case TRADE:
                publishEvent(tradesTopic, new BlockchainEvent(eventType, "BINANCE", data));
                break;
            case TICKER:
                publishEvent(tickersTopic, new BlockchainEvent(eventType, "BINANCE", data));
                break;
            default:
                break;
        }
    }

    private void publishEvent(String topic, BlockchainEvent event) {
        try {
            CompletableFuture<SendResult<String, BlockchainEvent>> future = kafkaTemplate.send(topic, event);

            future.thenAccept(result -> {
                long count = publishedEvents.incrementAndGet();
                logger.debug("Event published successfully to topic: {} |  EventId: {} | Total: {}",
                        topic, event.getEventId(), count);
            }).exceptionally(throwable -> {
                logger.error("Failed to publish event to topic: {} |  EventId: {} | Error: {}",
                        topic, event.getEventId(), throwable.getMessage());
                return null;
            });

        } catch (Exception e) {
            logger.error("Exception while publishing event: {}", e.getMessage(), e);
        }
    }

    public long getPublishedEventsCount() {
        return publishedEvents.get();
    }
}
