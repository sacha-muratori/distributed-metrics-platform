package com.streaming.repository.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "client_identity")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientIdentityDocument {

    @Id
    private String clientId;

    @Indexed(unique = true)
    private String fingerprint;
}
