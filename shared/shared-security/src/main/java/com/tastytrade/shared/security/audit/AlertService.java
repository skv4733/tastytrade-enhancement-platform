package com.tastytrade.shared.security.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AlertService {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertService.class);
    
    public void sendCriticalAlert(String message) {
        logger.error("CRITICAL ALERT: {}", message);
    }
    
    public void sendWarningAlert(String message) {
        logger.warn("WARNING ALERT: {}", message);
    }
    
    public void sendInfoAlert(String message) {
        logger.info("INFO ALERT: {}", message);
    }
}