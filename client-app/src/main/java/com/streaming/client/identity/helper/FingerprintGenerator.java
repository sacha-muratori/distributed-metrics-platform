package com.streaming.client.identity.helper;

import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.UUID;

public class FingerprintGenerator {

    public static String generate() {
        try {
            String os = System.getProperty("os.name", "unknown");
            String user = System.getProperty("user.name", "unknown");
            String host = System.getenv("HOSTNAME"); // Unix-like systems
            if (host == null) {
                host = System.getenv("COMPUTERNAME"); // Windows
            }

            StringBuilder macBuilder = new StringBuilder();
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                byte[] mac = ni.getHardwareAddress();
                if (mac != null) {
                    for (byte b : mac) {
                        macBuilder.append(String.format("%02X", b));
                    }
                    break; // Use the first valid MAC
                }
            }

            String raw = os + "_" + user + "_" + host + "_" + macBuilder;
            return UUID.nameUUIDFromBytes(raw.getBytes(StandardCharsets.UTF_8)).toString();

        } catch (Exception e) {
            // Fallback in case of failure
            return UUID.randomUUID().toString();
        }
    }
}
