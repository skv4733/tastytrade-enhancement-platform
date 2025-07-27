package com.tastytrade.shared.security.signature;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tastytrade.shared.models.transaction.FinancialTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class DigitalSignatureService {
    
    private static final Logger logger = LoggerFactory.getLogger(DigitalSignatureService.class);
    private static final String HMAC_SHA256 = "HmacSHA256";
    
    @Value("${tastytrade.security.signature.secret}")
    private String signatureSecret;
    
    private final ObjectMapper objectMapper;
    
    public DigitalSignatureService() {
        this.objectMapper = new ObjectMapper();
    }
    
    public String sign(FinancialTransaction transaction) {
        try {
            String transactionData = serializeForSigning(transaction);
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                signatureSecret.getBytes(StandardCharsets.UTF_8), 
                HMAC_SHA256
            );
            mac.init(secretKeySpec);
            byte[] signature = mac.doFinal(transactionData.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException | JsonProcessingException e) {
            logger.error("Error signing transaction: {}", transaction.getId(), e);
            throw new RuntimeException("Failed to sign transaction", e);
        }
    }
    
    public boolean verify(FinancialTransaction transaction, String expectedSignature) {
        try {
            String computedSignature = sign(transaction);
            return computedSignature.equals(expectedSignature);
        } catch (Exception e) {
            logger.error("Error verifying transaction signature: {}", transaction.getId(), e);
            return false;
        }
    }
    
    private String serializeForSigning(FinancialTransaction transaction) throws JsonProcessingException {
        SignatureData data = new SignatureData(
            transaction.getAccountNumber(),
            transaction.getSymbol(),
            transaction.getType(),
            transaction.getAmount(),
            transaction.getQuantity(),
            transaction.getPrice(),
            transaction.getCreatedDate()
        );
        return objectMapper.writeValueAsString(data);
    }
    
    private static class SignatureData {
        public final String accountNumber;
        public final String symbol;
        public final Object type;
        public final Object amount;
        public final Object quantity;
        public final Object price;
        public final Object createdDate;
        
        public SignatureData(String accountNumber, String symbol, Object type, 
                           Object amount, Object quantity, Object price, Object createdDate) {
            this.accountNumber = accountNumber;
            this.symbol = symbol;
            this.type = type;
            this.amount = amount;
            this.quantity = quantity;
            this.price = price;
            this.createdDate = createdDate;
        }
    }
}