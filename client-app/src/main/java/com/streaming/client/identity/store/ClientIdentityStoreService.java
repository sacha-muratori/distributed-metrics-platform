package com.streaming.client.identity.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaming.client.identity.model.ClientIdentity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@Slf4j
public class ClientIdentityStoreService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Path CLIENT_FILE =
            Path.of(System.getProperty("user.home"), ".distributed-metrics-platform", "client.json");

    private ClientIdentity cachedClient; // in-memory cache

    public ClientIdentityStoreService() {
        ensureDirectoryExists();
    }

    public synchronized ClientIdentity load() {
        if (cachedClient != null) {
            log.debug("Loaded ClientIdentity from cache");
            return cachedClient;
        }

        if (Files.exists(CLIENT_FILE)) {
            try {
                cachedClient = OBJECT_MAPPER.readValue(CLIENT_FILE.toFile(), ClientIdentity.class);
                log.debug("Loaded ClientIdentity from file");
                return cachedClient;
            } catch (IOException e) {
                log.warn("Failed to read client data, generating new: {}", e.getMessage());
            }
        }

        cachedClient = new ClientIdentity();
        log.debug("Created new ClientIdentity");
        return cachedClient;
    }

    public synchronized void save(ClientIdentity data) {
        try {
            OBJECT_MAPPER.writeValue(CLIENT_FILE.toFile(), data);
            cachedClient = data;
            log.debug("Saved ClientIdentity to {}", CLIENT_FILE);
        } catch (IOException e) {
            log.error("Failed to write client data: {}", e.getMessage());
        }
    }

    private void ensureDirectoryExists() {
        try {
            Files.createDirectories(CLIENT_FILE.getParent());
        } catch (IOException e) {
            throw new RuntimeException("Failed to create config directory: " + CLIENT_FILE.getParent(), e);
        }
    }
}
