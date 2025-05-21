package com.streaming.repository.model.entity;

import lombok.Data;

@Data
public class ClientIdentity {
    private String fingerprint;
    private String clientId;
}
