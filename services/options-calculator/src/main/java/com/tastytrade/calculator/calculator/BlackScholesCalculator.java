package com.tastytrade.calculator.calculator;

import com.tastytrade.calculator.model.BlackScholesParameters;
import com.tastytrade.calculator.model.OptionsGreeks;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@Component
public class BlackScholesCalculator {
    
    private static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);
    private final NormalDistribution normalDist = new NormalDistribution();

    public OptionsGreeks calculateGreeks(BlackScholesParameters params) {
        validateParameters(params);
        
        double S = params.getUnderlyingPrice().doubleValue();
        double K = params.getStrikePrice().doubleValue();
        double T = params.getTimeToExpiry().doubleValue();
        double r = params.getRiskFreeRate().doubleValue();
        double sigma = params.getVolatility().doubleValue();
        double q = params.getDividendYield().doubleValue();
        boolean isCall = "CALL".equalsIgnoreCase(params.getOptionType());

        // Calculate d1 and d2
        double d1 = (Math.log(S / K) + (r - q + 0.5 * sigma * sigma) * T) / (sigma * Math.sqrt(T));
        double d2 = d1 - sigma * Math.sqrt(T);

        // Calculate N(d1), N(d2), and their derivatives
        double Nd1 = normalDist.cumulativeProbability(d1);
        double Nd2 = normalDist.cumulativeProbability(d2);
        double nd1 = normalDist.density(d1); // PDF at d1
        double nd2 = normalDist.density(d2); // PDF at d2

        // Option price
        double optionPrice;
        if (isCall) {
            optionPrice = S * Math.exp(-q * T) * Nd1 - K * Math.exp(-r * T) * Nd2;
        } else {
            optionPrice = K * Math.exp(-r * T) * (1 - Nd2) - S * Math.exp(-q * T) * (1 - Nd1);
        }

        // Delta
        double delta;
        if (isCall) {
            delta = Math.exp(-q * T) * Nd1;
        } else {
            delta = Math.exp(-q * T) * (Nd1 - 1);
        }

        // Gamma (same for calls and puts)
        double gamma = Math.exp(-q * T) * nd1 / (S * sigma * Math.sqrt(T));

        // Theta
        double theta;
        double term1 = -S * nd1 * sigma * Math.exp(-q * T) / (2 * Math.sqrt(T));
        double term2 = q * S * Nd1 * Math.exp(-q * T);
        double term3 = r * K * Math.exp(-r * T) * Nd2;
        
        if (isCall) {
            theta = term1 - term2 - term3;
        } else {
            theta = term1 + q * S * (1 - Nd1) * Math.exp(-q * T) + r * K * Math.exp(-r * T) * (1 - Nd2);
        }
        theta = theta / 365.0; // Convert to daily theta

        // Vega (same for calls and puts)
        double vega = S * Math.exp(-q * T) * nd1 * Math.sqrt(T) / 100.0; // Convert to percentage

        // Rho
        double rho;
        if (isCall) {
            rho = K * T * Math.exp(-r * T) * Nd2 / 100.0;
        } else {
            rho = -K * T * Math.exp(-r * T) * (1 - Nd2) / 100.0;
        }

        return OptionsGreeks.builder()
                .optionPrice(BigDecimal.valueOf(optionPrice).round(MC))
                .delta(BigDecimal.valueOf(delta).round(MC))
                .gamma(BigDecimal.valueOf(gamma).round(MC))
                .theta(BigDecimal.valueOf(theta).round(MC))
                .vega(BigDecimal.valueOf(vega).round(MC))
                .rho(BigDecimal.valueOf(rho).round(MC))
                .underlyingPrice(params.getUnderlyingPrice())
                .strikePrice(params.getStrikePrice())
                .volatility(params.getVolatility())
                .timeToExpiry(params.getTimeToExpiry())
                .riskFreeRate(params.getRiskFreeRate())
                .optionType(params.getOptionType())
                .build();
    }

    public BigDecimal calculateImpliedVolatility(BlackScholesParameters params, BigDecimal marketPrice) {
        double targetPrice = marketPrice.doubleValue();
        double tolerance = 0.0001;
        int maxIterations = 100;
        
        // Initial guess
        double vol = 0.2;
        double volLow = 0.01;
        double volHigh = 5.0;
        
        for (int i = 0; i < maxIterations; i++) {
            BlackScholesParameters tempParams = BlackScholesParameters.builder()
                    .underlyingPrice(params.getUnderlyingPrice())
                    .strikePrice(params.getStrikePrice())
                    .timeToExpiry(params.getTimeToExpiry())
                    .riskFreeRate(params.getRiskFreeRate())
                    .volatility(BigDecimal.valueOf(vol))
                    .optionType(params.getOptionType())
                    .dividendYield(params.getDividendYield())
                    .build();
            
            OptionsGreeks greeks = calculateGreeks(tempParams);
            double calculatedPrice = greeks.getOptionPrice().doubleValue();
            double vega = greeks.getVega().doubleValue() * 100; // Convert back from percentage
            
            double priceDiff = calculatedPrice - targetPrice;
            
            if (Math.abs(priceDiff) < tolerance) {
                return BigDecimal.valueOf(vol).round(MC);
            }
            
            if (Math.abs(vega) < 1e-10) {
                break; // Avoid division by zero
            }
            
            // Newton-Raphson method
            double newVol = vol - priceDiff / vega;
            
            // Ensure the new volatility is within bounds
            if (newVol < volLow) {
                newVol = volLow;
            } else if (newVol > volHigh) {
                newVol = volHigh;
            }
            
            vol = newVol;
        }
        
        return BigDecimal.valueOf(vol).round(MC);
    }

    private void validateParameters(BlackScholesParameters params) {
        if (params.getUnderlyingPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Underlying price must be positive");
        }
        if (params.getStrikePrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Strike price must be positive");
        }
        if (params.getTimeToExpiry().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Time to expiry must be positive");
        }
        if (params.getVolatility().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Volatility must be positive");
        }
        if (!"CALL".equalsIgnoreCase(params.getOptionType()) && 
            !"PUT".equalsIgnoreCase(params.getOptionType())) {
            throw new IllegalArgumentException("Option type must be either CALL or PUT");
        }
    }
}