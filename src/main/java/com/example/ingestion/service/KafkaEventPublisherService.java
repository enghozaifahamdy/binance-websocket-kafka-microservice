package com.example.ingestion.service;

import com.example.ingestion.enums.EventTypeEnum;
import com.example.ingestion.event.BlockchainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

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

    public void publishEvent(EventTypeEnum eventType, Object data) {
        switch (eventType) {
            case TRADE:
                publishEvent(tradesTopic, new BlockchainEvent(eventType, "BINANCE", data));
                break;
            case TICKER:
                publishEvent(tickersTopic, new BlockchainEvent(eventType, "BINANCE", data));
                break;
            default:
                publishEvent(eventsTopic, new BlockchainEvent(eventType, "BINANCE", data));
                break;
        }
    }

    private void publishEvent(String topic, BlockchainEvent event) {
        try {
            kafkaTemplate.send(topic, event);
        } catch (Exception e) {
            logger.error("Exception while publishing event: {}", e.getMessage(), e);
        }
    }

}
