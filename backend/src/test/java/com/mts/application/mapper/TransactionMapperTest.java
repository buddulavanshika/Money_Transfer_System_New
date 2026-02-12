package com.mts.application.mapper;

import com.mts.application.entities.TransactionLog;
import com.mts.domain.dto.TransactionLogResponse;
import com.mts.domain.enums.TransactionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionMapperTest {

    @Test
    @DisplayName("Map TransactionLog entity to TransactionLogResponse")
    void toResponse_success() {
        TransactionLog entity = new TransactionLog();
        entity.setId("1");
        entity.setFromAccountId(1L);
        entity.setToAccountId(2L);
        entity.setAmount(new BigDecimal("100.00"));
        entity.setCurrency("USD");
        entity.setStatus(TransactionStatus.SUCCESS);
        entity.setFailureReason(null);
        entity.setIdempotencyKey(UUID.randomUUID().toString());
        entity.setCreatedOn(Instant.now());

        TransactionLogResponse response = TransactionMapper.toResponse(entity);

        assertNotNull(response);
        assertEquals(entity.getId(), response.id());
        assertEquals(entity.getFromAccountId(), response.fromAccountId());
        assertEquals(entity.getToAccountId(), response.toAccountId());
        assertEquals(entity.getAmount(), response.amount());
        assertEquals(entity.getCurrency(), response.currency());
        assertEquals(entity.getStatus(), response.status());
        assertEquals(entity.getFailureReason(), response.failureReason());
        assertEquals(entity.getIdempotencyKey(), response.idempotencyKey());
        assertEquals(entity.getCreatedOn(), response.createdOn());
    }

    @Test
    @DisplayName("Map null TransactionLog returns null")
    void toResponse_null() {
        TransactionLogResponse response = TransactionMapper.toResponse(null);

        assertNull(response);
    }

    @Test
    @DisplayName("Map TransactionLog with failure reason")
    void toResponse_withFailureReason() {
        TransactionLog entity = new TransactionLog();
        entity.setId("1");
        entity.setFromAccountId(1L);
        entity.setToAccountId(2L);
        entity.setAmount(new BigDecimal("100.00"));
        entity.setCurrency("USD");
        entity.setStatus(TransactionStatus.FAILED);
        entity.setFailureReason("Insufficient balance");
        entity.setIdempotencyKey(UUID.randomUUID().toString());
        entity.setCreatedOn(Instant.now());

        TransactionLogResponse response = TransactionMapper.toResponse(entity);

        assertNotNull(response);
        assertEquals(TransactionStatus.FAILED, response.status());
        assertEquals("Insufficient balance", response.failureReason());
    }
}

