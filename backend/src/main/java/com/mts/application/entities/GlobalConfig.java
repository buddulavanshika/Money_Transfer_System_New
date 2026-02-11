package com.mts.application.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "global_config")
public class GlobalConfig {

    @Id
    @Column(name = "config_key")
    private String id = "DEFAULT";

    @Column(name = "global_transfer_limit")
    private BigDecimal globalTransferLimit;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getGlobalTransferLimit() {
        return globalTransferLimit;
    }

    public void setGlobalTransferLimit(BigDecimal globalTransferLimit) {
        this.globalTransferLimit = globalTransferLimit;
    }
}
