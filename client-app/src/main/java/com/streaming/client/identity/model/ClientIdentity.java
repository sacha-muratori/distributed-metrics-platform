package com.streaming.client.identity.model;

import lombok.Data;

@Data
public class ClientIdentity {
    private String clientId;
    private String fingerprint;
}
