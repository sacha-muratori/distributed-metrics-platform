//package com.streaming.controller;
//
//import com.streaming.repository.dto.ClientDTO;
//import com.streaming.service.ClientService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import reactor.core.publisher.Mono;
//
//@RestController
//@RequestMapping("/api/client/")
//public class ClientController {
//
//    @Autowired
//    private ClientService clientService;
//
////    @PostMapping("/registration")
////    public Mono<ResponseEntity<Void>> clientRegistration(@RequestBody Mono<ClientDTO> metric) {
////        return metric
////            .flatMap(clientService::saveAggregatedMetric)
////            .thenReturn(ResponseEntity.ok().build());
////    }
//}