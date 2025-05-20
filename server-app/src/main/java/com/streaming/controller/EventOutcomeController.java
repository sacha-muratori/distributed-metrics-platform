//package com.bookmaker.controller;
//
//import com.bookmaker.model.dto.EventOutcome;
//import com.bookmaker.service.EventService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
///**
// * REST controller for managing event outcomes.
// * Provides an endpoint to publish event outcomes to the EventService.
// */
//@RestController
//@RequestMapping("/api/events")
//public class EventOutcomeController {
//
//    @Autowired
//    private EventService eventService;
//
//    @PostMapping("/publish")
//    public ResponseEntity<String> publishEventOutcome(@RequestBody EventOutcome eventOutcome) {
//        eventService.sendEventOutcome(eventOutcome);
//        return ResponseEntity.ok("Event processed successfully towards Kafka topic");
//    }
//}



// TODO need to expose a get endpoint for the properties