-- Initialize TimescaleDB extension
CREATE EXTENSION IF NOT EXISTS timescaledb CASCADE;

-- Create schemas
CREATE SCHEMA IF NOT EXISTS trading;
CREATE SCHEMA IF NOT EXISTS audit;
CREATE SCHEMA IF NOT EXISTS monitoring;

-- Grant permissions
GRANT ALL PRIVILEGES ON SCHEMA trading TO tastytrade;
GRANT ALL PRIVILEGES ON SCHEMA audit TO tastytrade;
GRANT ALL PRIVILEGES ON SCHEMA monitoring TO tastytrade;

-- Create market data hypertable for time-series data
CREATE TABLE IF NOT EXISTS trading.market_data (
    time TIMESTAMPTZ NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    price DECIMAL(19,4),
    volume BIGINT,
    bid_price DECIMAL(19,4),
    ask_price DECIMAL(19,4),
    bid_size INTEGER,
    ask_size INTEGER,
    underlying_price DECIMAL(19,4),
    implied_volatility DECIMAL(10,6),
    delta DECIMAL(10,6),
    gamma DECIMAL(10,6),
    theta DECIMAL(10,6),
    vega DECIMAL(10,6),
    rho DECIMAL(10,6),
    open_interest INTEGER,
    strike_price DECIMAL(19,4),
    expiration_date DATE,
    option_type VARCHAR(4)
);

-- Convert to hypertable
SELECT create_hypertable('trading.market_data', 'time', if_not_exists => TRUE);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_market_data_symbol_time ON trading.market_data (symbol, time DESC);
CREATE INDEX IF NOT EXISTS idx_market_data_underlying ON trading.market_data (underlying_price, time DESC) WHERE underlying_price IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_market_data_options ON trading.market_data (symbol, expiration_date, strike_price) WHERE option_type IS NOT NULL;

-- Create delta monitoring table
CREATE TABLE IF NOT EXISTS monitoring.delta_alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    symbol VARCHAR(20) NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    current_delta DECIMAL(10,6),
    threshold_delta DECIMAL(10,6),
    breach_type VARCHAR(20),
    alert_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    resolved BOOLEAN DEFAULT FALSE,
    resolved_time TIMESTAMPTZ
);

-- Convert to hypertable
SELECT create_hypertable('monitoring.delta_alerts', 'alert_time', if_not_exists => TRUE);

-- Create audit log table for compliance
CREATE TABLE IF NOT EXISTS audit.transaction_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    details JSONB,
    user_id VARCHAR(100),
    ip_address INET,
    user_agent TEXT,
    timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Convert to hypertable
SELECT create_hypertable('audit.transaction_log', 'timestamp', if_not_exists => TRUE);

-- Create compression policy (compress data older than 7 days)
SELECT add_compression_policy('trading.market_data', INTERVAL '7 days', if_not_exists => TRUE);
SELECT add_compression_policy('monitoring.delta_alerts', INTERVAL '30 days', if_not_exists => TRUE);
SELECT add_compression_policy('audit.transaction_log', INTERVAL '90 days', if_not_exists => TRUE);

-- Create retention policy (delete data older than 2 years for market data)
SELECT add_retention_policy('trading.market_data', INTERVAL '2 years', if_not_exists => TRUE);
SELECT add_retention_policy('monitoring.delta_alerts', INTERVAL '5 years', if_not_exists => TRUE);

-- Create materialized views for analytics
CREATE MATERIALIZED VIEW IF NOT EXISTS trading.hourly_market_stats
WITH (timescaledb.continuous) AS
SELECT
    time_bucket('1 hour', time) AS bucket,
    symbol,
    first(price, time) AS open_price,
    max(price) AS high_price,
    min(price) AS low_price,
    last(price, time) AS close_price,
    sum(volume) AS total_volume,
    avg(implied_volatility) AS avg_iv,
    avg(delta) AS avg_delta
FROM trading.market_data
WHERE price IS NOT NULL
GROUP BY bucket, symbol;

-- Add refresh policy for continuous aggregates
SELECT add_continuous_aggregate_policy('trading.hourly_market_stats',
    start_offset => INTERVAL '3 hours',
    end_offset => INTERVAL '1 hour',
    schedule_interval => INTERVAL '1 hour',
    if_not_exists => TRUE);

-- Create user-defined functions for Greeks calculations
CREATE OR REPLACE FUNCTION trading.calculate_portfolio_delta(account_num VARCHAR)
RETURNS DECIMAL AS $$
DECLARE
    total_delta DECIMAL := 0;
BEGIN
    -- This would be implemented to calculate portfolio delta
    -- Placeholder for actual implementation
    RETURN total_delta;
END;
$$ LANGUAGE plpgsql;

-- Create function to check delta thresholds
CREATE OR REPLACE FUNCTION monitoring.check_delta_threshold(
    p_symbol VARCHAR,
    p_current_delta DECIMAL,
    p_threshold DECIMAL
) RETURNS BOOLEAN AS $$
BEGIN
    RETURN ABS(p_current_delta) > ABS(p_threshold);
END;
$$ LANGUAGE plpgsql;

-- Grant execute permissions
GRANT EXECUTE ON FUNCTION trading.calculate_portfolio_delta(VARCHAR) TO tastytrade;
GRANT EXECUTE ON FUNCTION monitoring.check_delta_threshold(VARCHAR, DECIMAL, DECIMAL) TO tastytrade;

-- Create RLS policies for multi-tenant security
ALTER TABLE trading.market_data ENABLE ROW LEVEL SECURITY;
ALTER TABLE monitoring.delta_alerts ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit.transaction_log ENABLE ROW LEVEL SECURITY;

-- Create policy for market data (read access for authenticated users)
CREATE POLICY market_data_access ON trading.market_data
    FOR ALL
    TO tastytrade
    USING (true);

-- Create policy for delta alerts (users can only see their own alerts)
CREATE POLICY delta_alerts_access ON monitoring.delta_alerts
    FOR ALL
    TO tastytrade
    USING (account_number = current_setting('app.current_account', true));

-- Create policy for audit logs (restricted access)
CREATE POLICY audit_log_access ON audit.transaction_log
    FOR SELECT
    TO tastytrade
    USING (account_number = current_setting('app.current_account', true));

-- Optimize database settings for time-series workloads
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';
ALTER SYSTEM SET maintenance_work_mem = '64MB';
ALTER SYSTEM SET checkpoint_completion_target = 0.9;
ALTER SYSTEM SET wal_buffers = '16MB';
ALTER SYSTEM SET default_statistics_target = 100;

-- Create indexes for better performance
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_delta_alerts_symbol_time 
    ON monitoring.delta_alerts (symbol, alert_time DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_delta_alerts_account 
    ON monitoring.delta_alerts (account_number, alert_time DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_audit_log_account_time 
    ON audit.transaction_log (account_number, timestamp DESC);

COMMIT;