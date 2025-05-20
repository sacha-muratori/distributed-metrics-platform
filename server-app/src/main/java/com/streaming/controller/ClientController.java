//package com.streaming.controller;
//
//@RestController
//@RequestMapping("/api/client")
//public class ClientController {
//
//    @Autowired
//    private ClientRepository clientRepository;
//
//    @PostMapping("/register")
//    public ResponseEntity<Map<String, String>> register(@RequestBody Map<String, String> body) {
//        String fingerprint = body.get("fingerprint");
//        Client client = clientRepository.findByFingerprint(fingerprint)
//            .orElseGet(() -> {
//                Client newClient = new Client();
//                newClient.setId(UUID.randomUUID().toString());
//                newClient.setFingerprint(fingerprint);
//                return clientRepository.save(newClient);
//            });
//
//        return ResponseEntity.ok(Map.of("clientId", client.getId()));
//    }
//}
