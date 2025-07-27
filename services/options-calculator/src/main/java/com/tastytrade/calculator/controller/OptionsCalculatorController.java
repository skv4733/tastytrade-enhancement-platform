package com.tastytrade.calculator.controller;

import com.tastytrade.calculator.dto.GreeksCalculationRequest;
import com.tastytrade.calculator.model.BlackScholesParameters;
import com.tastytrade.calculator.model.OptionsGreeks;
import com.tastytrade.calculator.service.OptionsCalculatorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/options")
@CrossOrigin(origins = "*")
public class OptionsCalculatorController {
    
    @Autowired
    private OptionsCalculatorService calculatorService;

    @PostMapping("/greeks")
    public ResponseEntity<OptionsGreeks> calculateGreeks(@Valid @RequestBody GreeksCalculationRequest request) {
        BigDecimal timeToExpiry = calculatorService.calculateTimeToExpiry(request.getExpirationDate());
        
        BlackScholesParameters params = BlackScholesParameters.builder()
                .underlyingPrice(request.getUnderlyingPrice())
                .strikePrice(request.getStrikePrice())
                .timeToExpiry(timeToExpiry)
                .riskFreeRate(request.getRiskFreeRate())
                .volatility(request.getVolatility())
                .optionType(request.getOptionType())
                .dividendYield(request.getDividendYield())
                .build();
        
        OptionsGreeks greeks = calculatorService.calculateGreeks(params);
        greeks.setSymbol(request.getSymbol());
        
        return ResponseEntity.ok(greeks);
    }

    @PostMapping("/greeks/async")
    public CompletableFuture<ResponseEntity<OptionsGreeks>> calculateGreeksAsync(
            @Valid @RequestBody GreeksCalculationRequest request) {
        BigDecimal timeToExpiry = calculatorService.calculateTimeToExpiry(request.getExpirationDate());
        
        BlackScholesParameters params = BlackScholesParameters.builder()
                .underlyingPrice(request.getUnderlyingPrice())
                .strikePrice(request.getStrikePrice())
                .timeToExpiry(timeToExpiry)
                .riskFreeRate(request.getRiskFreeRate())
                .volatility(request.getVolatility())
                .optionType(request.getOptionType())
                .dividendYield(request.getDividendYield())
                .build();
        
        return calculatorService.calculateGreeksAsync(params)
                .thenApply(greeks -> {
                    greeks.setSymbol(request.getSymbol());
                    return ResponseEntity.ok(greeks);
                });
    }

    @PostMapping("/implied-volatility")
    public ResponseEntity<BigDecimal> calculateImpliedVolatility(
            @Valid @RequestBody GreeksCalculationRequest request,
            @RequestParam BigDecimal marketPrice) {
        
        BigDecimal timeToExpiry = calculatorService.calculateTimeToExpiry(request.getExpirationDate());
        
        BlackScholesParameters params = BlackScholesParameters.builder()
                .underlyingPrice(request.getUnderlyingPrice())
                .strikePrice(request.getStrikePrice())
                .timeToExpiry(timeToExpiry)
                .riskFreeRate(request.getRiskFreeRate())
                .volatility(request.getVolatility()) // Initial guess
                .optionType(request.getOptionType())
                .dividendYield(request.getDividendYield())
                .build();
        
        BigDecimal impliedVol = calculatorService.calculateImpliedVolatility(params, marketPrice);
        
        return ResponseEntity.ok(impliedVol);
    }

    @GetMapping("/delta-hedge")
    public ResponseEntity<BigDecimal> calculateDeltaHedge(
            @RequestParam BigDecimal delta,
            @RequestParam BigDecimal quantity) {
        
        OptionsGreeks greeks = OptionsGreeks.builder()
                .delta(delta)
                .build();
        
        BigDecimal hedgeRatio = calculatorService.calculateDeltaHedgeRatio(greeks, quantity);
        
        return ResponseEntity.ok(hedgeRatio);
    }

    @GetMapping("/gamma-scaling")
    public ResponseEntity<BigDecimal> calculateGammaScaling(
            @RequestParam BigDecimal gamma,
            @RequestParam BigDecimal priceMove) {
        
        OptionsGreeks greeks = OptionsGreeks.builder()
                .gamma(gamma)
                .build();
        
        BigDecimal deltaChange = calculatorService.calculateGammaScaling(greeks, priceMove);
        
        return ResponseEntity.ok(deltaChange);
    }

    @GetMapping("/theta-decay")
    public ResponseEntity<BigDecimal> calculateThetaDecay(
            @RequestParam BigDecimal theta,
            @RequestParam int days) {
        
        OptionsGreeks greeks = OptionsGreeks.builder()
                .theta(theta)
                .build();
        
        BigDecimal priceDecay = calculatorService.calculateThetaDecay(greeks, days);
        
        return ResponseEntity.ok(priceDecay);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Options Calculator Service is running");
    }
}