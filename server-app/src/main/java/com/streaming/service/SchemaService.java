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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class SchemaService {

    @Autowired
    private ObjectMapper objectMapper;

    public record ValidatedLine(Map<String, Object> raw, Set<ValidationMessage> errors) {}

    public JsonSchema generateSchema(List<String> enabledStrategies) {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");
        ObjectNode properties = objectMapper.createObjectNode();

        List<String> required = List.of("fingerprint", "timestamp");  // clientId is optional (client not registered yet)

        properties.putObject("fingerprint").put("type", "string");

        // Allow clientId to be string or null
        properties.putObject("clientId").putArray("type").add("string").add("null");

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

        // fingerprint and clientId are strings, just null-check presence
        if (input.containsKey("fingerprint")) {
            doc.setFingerprint((String) input.get("fingerprint"));
        }
        if (input.containsKey("clientId")) {
            doc.setClientId((String) input.get("clientId"));
        }
        if (input.containsKey("timestamp")) {
            String tsString = (String) input.get("timestamp");
            if (tsString != null) {
                doc.setTimestamp(Instant.parse(tsString));
            }
        }
        if (input.containsKey("availableProcessors")) {
            Object val = input.get("availableProcessors");
            if (val != null) {
                doc.setAvailableProcessors((Integer) val);
            }
        }
        if (input.containsKey("systemCpuUsagePercent")) {
            Object val = input.get("systemCpuUsagePercent");
            if (val != null) {
                doc.setSystemCpuUsagePercent(((Number) val).doubleValue());
            }
        }
        if (input.containsKey("usableDiskBytes")) {
            Object val = input.get("usableDiskBytes");
            if (val != null) {
                doc.setUsableDiskBytes(((Number) val).longValue());
            }
        }
        if (input.containsKey("totalDiskBytes")) {
            Object val = input.get("totalDiskBytes");
            if (val != null) {
                doc.setTotalDiskBytes(((Number) val).longValue());
            }
        }
        if (input.containsKey("freeDiskBytes")) {
            Object val = input.get("freeDiskBytes");
            if (val != null) {
                doc.setFreeDiskBytes(((Number) val).longValue());
            }
        }
        if (input.containsKey("totalPhysicalMemoryBytes")) {
            Object val = input.get("totalPhysicalMemoryBytes");
            if (val != null) {
                doc.setTotalPhysicalMemoryBytes(((Number) val).longValue());
            }
        }
        if (input.containsKey("freePhysicalMemoryBytes")) {
            Object val = input.get("freePhysicalMemoryBytes");
            if (val != null) {
                doc.setFreePhysicalMemoryBytes(((Number) val).longValue());
            }
        }
        if (input.containsKey("usedPhysicalMemoryBytes")) {
            Object val = input.get("usedPhysicalMemoryBytes");
            if (val != null) {
                doc.setUsedPhysicalMemoryBytes(((Number) val).longValue());
            }
        }
        if (input.containsKey("hostname")) {
            doc.setHostname((String) input.get("hostname"));
        }
        if (input.containsKey("ipaddress")) {
            doc.setIpaddress((String) input.get("ipaddress"));
        }
        return doc;
    }
}
