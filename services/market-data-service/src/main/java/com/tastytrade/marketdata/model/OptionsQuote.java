package com.tastytrade.marketdata.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "options_quotes")
public class OptionsQuote {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String symbol;
    
    @Column(nullable = false)
    private BigDecimal price;
    
    @Column(name = "underlying_price")
    private BigDecimal underlyingPrice;
    
    @Column(name = "strike_price")
    private BigDecimal strikePrice;
    
    @Column(name = "bid_price")
    private BigDecimal bidPrice;
    
    @Column(name = "ask_price")
    private BigDecimal askPrice;
    
    @Column(name = "bid_size")
    private Integer bidSize;
    
    @Column(name = "ask_size")
    private Integer askSize;
    
    private Long volume;
    
    @Column(name = "open_interest")
    private Integer openInterest;
    
    @Column(name = "implied_volatility", precision = 10, scale = 6)
    private BigDecimal impliedVolatility;
    
    @Column(precision = 10, scale = 6)
    private BigDecimal delta;
    
    @Column(precision = 10, scale = 6)
    private BigDecimal gamma;
    
    @Column(precision = 10, scale = 6)
    private BigDecimal theta;
    
    @Column(precision = 10, scale = 6)
    private BigDecimal vega;
    
    @Column(precision = 10, scale = 6)
    private BigDecimal rho;
    
    @Column(name = "option_type")
    private String optionType;
    
    @CreatedDate
    @Column(name = "quote_time")
    private Instant quoteTime;

    public OptionsQuote() {}

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getUnderlyingPrice() { return underlyingPrice; }
    public void setUnderlyingPrice(BigDecimal underlyingPrice) { this.underlyingPrice = underlyingPrice; }

    public BigDecimal getStrikePrice() { return strikePrice; }
    public void setStrikePrice(BigDecimal strikePrice) { this.strikePrice = strikePrice; }

    public BigDecimal getBidPrice() { return bidPrice; }
    public void setBidPrice(BigDecimal bidPrice) { this.bidPrice = bidPrice; }

    public BigDecimal getAskPrice() { return askPrice; }
    public void setAskPrice(BigDecimal askPrice) { this.askPrice = askPrice; }

    public Integer getBidSize() { return bidSize; }
    public void setBidSize(Integer bidSize) { this.bidSize = bidSize; }

    public Integer getAskSize() { return askSize; }
    public void setAskSize(Integer askSize) { this.askSize = askSize; }

    public Long getVolume() { return volume; }
    public void setVolume(Long volume) { this.volume = volume; }

    public Integer getOpenInterest() { return openInterest; }
    public void setOpenInterest(Integer openInterest) { this.openInterest = openInterest; }

    public BigDecimal getImpliedVolatility() { return impliedVolatility; }
    public void setImpliedVolatility(BigDecimal impliedVolatility) { this.impliedVolatility = impliedVolatility; }

    public BigDecimal getDelta() { return delta; }
    public void setDelta(BigDecimal delta) { this.delta = delta; }

    public BigDecimal getGamma() { return gamma; }
    public void setGamma(BigDecimal gamma) { this.gamma = gamma; }

    public BigDecimal getTheta() { return theta; }
    public void setTheta(BigDecimal theta) { this.theta = theta; }

    public BigDecimal getVega() { return vega; }
    public void setVega(BigDecimal vega) { this.vega = vega; }

    public BigDecimal getRho() { return rho; }
    public void setRho(BigDecimal rho) { this.rho = rho; }

    public String getOptionType() { return optionType; }
    public void setOptionType(String optionType) { this.optionType = optionType; }

    public Instant getQuoteTime() { return quoteTime; }
    public void setQuoteTime(Instant quoteTime) { this.quoteTime = quoteTime; }

    public Instant getTimestamp() { return quoteTime; }
}