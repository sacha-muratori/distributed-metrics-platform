package com.streaming.client.identity.helper;

import java.util.UUID;

public class FingerprintGenerator {

    public static String generate() {
        return UUID.randomUUID().toString();
    }
}
