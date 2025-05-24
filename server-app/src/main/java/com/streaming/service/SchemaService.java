package com.streaming.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import com.streaming.repository.model.MetricsDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchemaService {

    @Autowired
    private ObjectMapper objectMapper;

    public record ValidatedLine(Map<String, Object> raw, Set<ValidationMessage> errors) {}

    public JsonSchema generateSchema(List<String> enabledStrategies) {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");
        ObjectNode properties = objectMapper.createObjectNode();
        List<String> required = List.of("fingerprint", "clientId", "timestamp");

        properties.putObject("fingerprint").put("type", "string");
        properties.putObject("clientId").put("type", "string");
        properties.putObject("timestamp").put("type", "string").put("format", "date-time");

        if (enabledStrategies.contains("cpu")) {
            properties.putObject("availableProcessors").put("type", "integer");
            properties.putObject("systemCpuUsagePercent").put("type", "number");
        }

        if (enabledStrategies.contains("disk")) {
            properties.putObject("usableDiskBytes").put("type", "integer");
            properties.putObject("totalDiskBytes").put("type", "integer");
            properties.putObject("freeDiskBytes").put("type", "integer");
        }

        if (enabledStrategies.contains("memory")) {
            properties.putObject("totalPhysicalMemoryBytes").put("type", "integer");
            properties.putObject("freePhysicalMemoryBytes").put("type", "integer");
            properties.putObject("usedPhysicalMemoryBytes").put("type", "integer");
        }

        if (enabledStrategies.contains("hostname")) {
            properties.putObject("hostname").put("type", "string");
            properties.putObject("ipaddress").put("type", "string").put("format", "ipv4");
        }

        schema.set("properties", properties);
        schema.putPOJO("required", required);

        return JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7).getSchema(schema);
    }

    public Set<ValidationMessage> validate(Map<String, Object> input, List<String> strategies) {
        try {
            JsonSchema schema = generateSchema(strategies);
            return schema.validate(objectMapper.valueToTree(input));
        } catch (Exception e) {
            return Set.of(ValidationMessage.builder()
                    .type("exception")
                    .message("Schema validation exception: " + e.getMessage())
                    .build());
        }
    }

    public List<ValidatedLine> parseAndValidateEachLine(byte[] input, List<String> strategies) {
        List<ValidatedLine> result = new ArrayList<>();
        JsonSchema schema = generateSchema(strategies);

        try (ByteArrayInputStream stream = new ByteArrayInputStream(input);
             JsonParser parser = new JsonFactory(objectMapper).createParser(stream)) {

            while (!parser.isClosed()) {
                JsonToken token = parser.nextToken();
                if (token == JsonToken.START_OBJECT) {
                    Map<String, Object> map = objectMapper.readValue(parser, new TypeReference<>() {});
                    Set<ValidationMessage> errors = schema.validate(objectMapper.valueToTree(map));
                    result.add(new ValidatedLine(map, errors));
                }
            }

        } catch (IOException e) {
            result.add(new ValidatedLine(null, Set.of(ValidationMessage.builder()
                    .type("exception")
                    .message("Validation parse error: " + e.getMessage())
                    .build())));
        }

        return result;
    }

    public MetricsDocument toMetricsDocument(Map<String, Object> input) {
        MetricsDocument doc = new MetricsDocument();

        if (input == null) return doc;

        doc.setFingerprint((String) input.get("fingerprint"));
        doc.setClientId((String) input.get("clientId"));

        String tsString = (String) input.get("timestamp");
        Instant timestamp = Instant.parse(tsString);
        doc.setTimestamp(timestamp);

        doc.setAvailableProcessors((Integer) input.get("availableProcessors"));
        doc.setSystemCpuUsagePercent(((Number) input.get("systemCpuUsagePercent")).doubleValue());

        doc.setUsableDiskBytes(((Number) input.get("usableDiskBytes")).longValue());
        doc.setTotalDiskBytes(((Number) input.get("totalDiskBytes")).longValue());
        doc.setFreeDiskBytes(((Number) input.get("freeDiskBytes")).longValue());

        doc.setTotalPhysicalMemoryBytes(((Number) input.get("totalPhysicalMemoryBytes")).longValue());
        doc.setFreePhysicalMemoryBytes(((Number) input.get("freePhysicalMemoryBytes")).longValue());
        doc.setUsedPhysicalMemoryBytes(((Number) input.get("usedPhysicalMemoryBytes")).longValue());

        doc.setHostname((String) input.get("hostname"));
        doc.setIpaddress((String) input.get("ipaddress"));

        return doc;
    }

    public List<MetricsDocument> toMetricsDocumentList(byte[] metrics) {
        List<MetricsDocument> docs = new ArrayList<>();
        try (ByteArrayInputStream stream = new ByteArrayInputStream(metrics);
             JsonParser parser = new JsonFactory(objectMapper).createParser(stream)) {

            while (!parser.isClosed()) {
                JsonToken token = parser.nextToken();
                if (token == JsonToken.START_OBJECT) {
                    Map<String, Object> map = objectMapper.readValue(parser, new TypeReference<>() {});
                    docs.add(toMetricsDocument(map));
                }
            }

        } catch (IOException e) {
            log.error("Error parsing metrics JSON lines to documents: {}", e.getMessage());
        }
        return docs;
    }
}
