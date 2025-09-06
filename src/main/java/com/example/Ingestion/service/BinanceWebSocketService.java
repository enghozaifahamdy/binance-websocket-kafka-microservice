package com.example.Ingestion.service;


import com.example.Ingestion.enums.EventTypeEnum;
import com.example.Ingestion.event.TickerEvent;
import com.example.Ingestion.event.TradeEvent;
import com.example.Ingestion.mapper.EventMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Arrays;
import java.util.List;

@Service
public class BinanceWebSocketService {

    private static final Logger logger = LoggerFactory.getLogger(BinanceWebSocketService.class);
    @Autowired
    private KafkaEventPublisherService eventPublisher;
    @Autowired
    private EventMapper eventMapper;
    private WebSocketClient client;
    private ObjectMapper objectMapper = new ObjectMapper();

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    @Value("${binance.websocket.url}")
    private String BINANCE_WS_URL;

    @Value("${binance.symbols}")
    private String symbolsConfig;

    @Value("${binance.stream-types}")
    private String streamTypesConfig;

    private final AtomicLong receivedMessages = new AtomicLong(0);
    private final AtomicLong processedTrades = new AtomicLong(0);
    private final AtomicLong processedTickers = new AtomicLong(0);

    @PostConstruct
    public void init() {
        logger.info("Initializing Binance WebSocket Microservice");

        List<String> symbols = Arrays.asList(symbolsConfig.split(","));
        List<String> streamTypes = Arrays.asList(streamTypesConfig.split(","));

        logger.info("Configured symbols: {}", symbols);
        logger.info("Configured stream types: {}", streamTypes);

        connectToStreams(symbols, streamTypes);
        startHealthMonitoring();
        startMetricsLogging();
    }

    private void connectToStreams(List<String> symbols, List<String> streamTypes) {
        try {
            StringBuilder streamsBuilder = new StringBuilder();
            boolean first = true;

            for (String symbol : symbols) {
                for (String streamType : streamTypes) {
                    if (!first) {
                        streamsBuilder.append("/");
                    }
                    streamsBuilder.append(symbol.toLowerCase().trim()).append("@").append(streamType.trim());
                    first = false;
                }
            }

            String streamUrl = BINANCE_WS_URL + streamsBuilder.toString();
            URI uri = new URI(streamUrl);

            logger.info("Connecting to: {}", streamUrl);

            client = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    logger.info("Connected to Binance WebSocket");
                    logger.info("Handshake - Status: {} | Message: {}",
                            handshake.getHttpStatus(), handshake.getHttpStatusMessage());
                }

                @Override
                public void onMessage(String message) {
                    try {
                        logger.info("Received message: {}", message);
                        receivedMessages.incrementAndGet();
                        processIncomingMessage(message);
                    } catch (Exception e) {
                        logger.error("Error processing message: {}", e.getMessage());
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    logger.warn("WebSocket connection closed - Code: {} | Reason: {} | Remote: {}",
                            code, reason, remote);
                    scheduleReconnect();
                }

                @Override
                public void onError(Exception ex) {
                    logger.error("WebSocket error: {}", ex.getMessage());
                }
            };

            client.connect();

        } catch (Exception e) {
            logger.error("Failed to connect to Binance WebSocket: {}", e.getMessage());
            scheduleReconnect();
        }
    }

    private void processIncomingMessage(String message) {
        try {
            // Parse the JSON to determine event type
            JsonNode jsonNode = objectMapper.readTree(message);
            String eventType = jsonNode.get("e").asText();
            String symbol = jsonNode.get("s").asText();

            logger.debug("Received {} event for {}", eventType, symbol);

            switch (eventType) {
                case "trade":
                    TradeEvent tradeEvent = eventMapper.tradeToKafkaEvent(jsonNode);
                    eventPublisher.publishEvent(EventTypeEnum.TRADE, tradeEvent);
                    processedTrades.incrementAndGet();
                    logger.debug("Trade: {} | Price: {} | Quantity: {} | Buyer MM: {}",
                            symbol, tradeEvent.getPrice(), tradeEvent.getQuantity(), tradeEvent.getIsBuyerMarketMaker());
                    break;

                case "24hrTicker":
                    TickerEvent tickerEvent = eventMapper.tickerToKafkaEvent(jsonNode);
                    eventPublisher.publishEvent(EventTypeEnum.TICKER, tickerEvent);
                    processedTickers.incrementAndGet();
                    logger.debug("Ticker: {} | Price: {} | Change: {}%",
                            symbol, tickerEvent.getLastPrice(), tickerEvent.getPriceChangePercent());
                    break;

                default:
                    // Handle other event types
                    eventPublisher.publishEvent(EventTypeEnum.UNKNOWN, jsonNode);
                    logger.debug("Generic event: {} for {}", eventType, symbol);
            }

        } catch (Exception e) {
            logger.error("Failed to process message: {} | Raw: {}", e.getMessage(), message);
        }
    }

    private void scheduleReconnect() {
        scheduler.schedule(() -> {
            logger.info("Attempting to reconnect...");
            init();
        }, 5, TimeUnit.SECONDS);
    }

    private void startHealthMonitoring() {
        scheduler.scheduleAtFixedRate(() -> {
            if (client == null || client.isClosed()) {
                logger.warn("WebSocket connection is down, attempting reconnect...");
                scheduleReconnect();
            } else {
                logger.info("Health Check - Connection: HEALTHY | Messages: {} ",
                        receivedMessages.get());
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    private void startMetricsLogging() {
        scheduler.scheduleAtFixedRate(() -> {
            logger.info("METRICS - Received: {} | Trades: {} | Tickers: {} ",
                    receivedMessages.get(), processedTrades.get(), processedTickers.get());
        }, 60, 60, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down Binance WebSocket Service");

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }

        if (client != null && client.isOpen()) {
            client.close();
        }

        logger.info("Shutdown complete");
    }
}