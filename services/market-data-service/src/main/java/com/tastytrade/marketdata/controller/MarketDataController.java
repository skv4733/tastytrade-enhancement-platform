package com.tastytrade.marketdata.controller;

import com.tastytrade.marketdata.model.OptionsQuote;
import com.tastytrade.marketdata.service.MarketDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/market-data")
@CrossOrigin(origins = "*")
public class MarketDataController {
    
    private static final Logger logger = LoggerFactory.getLogger(MarketDataController.class);
    
    @Autowired
    private MarketDataService marketDataService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping(value = "/stream/{symbol}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<OptionsQuote>> streamQuotes(@PathVariable String symbol) {
        logger.info("Starting quote stream for symbol: {}", symbol);
        
        return marketDataService.getQuoteStream(symbol)
                .map(quote -> ServerSentEvent.builder(quote)
                        .id(String.valueOf(quote.getTimestamp().toEpochMilli()))
                        .event("quote-update")
                        .build())
                .doOnNext(quote -> {
                    // Also send via WebSocket for clients that prefer WebSocket over SSE
                    messagingTemplate.convertAndSend("/topic/quotes/" + symbol, quote.data());
                })
                .doOnError(error -> logger.error("Error in quote stream for symbol: {}", symbol, error))
                .onErrorResume(error -> Flux.empty());
    }

    @GetMapping(value = "/realtime/{symbol}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<OptionsQuote>> streamRealtimeQuotes(@PathVariable String symbol) {
        logger.info("Starting realtime quote stream for symbol: {}", symbol);
        
        return marketDataService.getRealtimeQuotes(symbol)
                .map(quote -> ServerSentEvent.builder(quote)
                        .id(String.valueOf(quote.getTimestamp().toEpochMilli()))
                        .event("realtime-quote")
                        .build())
                .doOnNext(quote -> logger.debug("Streaming quote for {}: {}", symbol, quote.data().getPrice()))
                .doOnError(error -> logger.error("Error in realtime stream for symbol: {}", symbol, error))
                .onErrorResume(error -> Flux.empty());
    }

    @GetMapping("/latest/{symbol}")
    public Mono<OptionsQuote> getLatestQuote(@PathVariable String symbol) {
        return marketDataService.getLatestQuote(symbol);
    }

    @GetMapping("/historical/{symbol}")
    public Flux<OptionsQuote> getHistoricalQuotes(
            @PathVariable String symbol,
            @RequestParam(required = false) String fromTime,
            @RequestParam(required = false) String toTime) {
        
        Instant from = fromTime != null ? Instant.parse(fromTime) : 
                       Instant.now().minus(1, ChronoUnit.DAYS);
        Instant to = toTime != null ? Instant.parse(toTime) : Instant.now();
        
        return marketDataService.getHistoricalQuotes(symbol, from, to);
    }

    @PostMapping("/quotes")
    public Mono<Void> publishQuote(@RequestBody OptionsQuote quote) {
        return marketDataService.publishQuote(quote);
    }

    @PostMapping("/start-simulation")
    public Mono<String> startSimulation() {
        return marketDataService.startMarketDataSimulation()
                .then(Mono.just("Market data simulation started"));
    }

    @GetMapping("/subscribers/{symbol}")
    public Mono<Long> getSubscriberCount(@PathVariable String symbol) {
        return marketDataService.getActiveSubscriberCount(symbol);
    }

    @PostMapping("/cleanup")
    public Mono<String> cleanupStreams() {
        return marketDataService.cleanupInactiveStreams()
                .then(Mono.just("Cleanup completed"));
    }

    @GetMapping(value = "/heartbeat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> heartbeat() {
        return Flux.interval(Duration.ofSeconds(30))
                .map(sequence -> ServerSentEvent.builder("ping")
                        .id(String.valueOf(sequence))
                        .event("heartbeat")
                        .build());
    }

    @GetMapping("/health")
    public Mono<String> healthCheck() {
        return Mono.just("Market Data Service is running");
    }
}