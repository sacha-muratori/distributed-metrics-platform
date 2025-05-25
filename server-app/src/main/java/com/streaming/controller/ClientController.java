package com.streaming.controller;

import com.streaming.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/client")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @PostMapping("/registration")
    public Mono<ResponseEntity<Map<String, Object>>> clientRegistration(@RequestBody Map<String, Object> requestBody) {
        return clientService.registerClient(requestBody).map(ResponseEntity::ok);
    }
}