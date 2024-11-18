package us.chaoqun.ethereum.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import us.chaoqun.ethereum.demo.model.TransactionRequest;
import us.chaoqun.ethereum.demo.model.TransactionResponse;
import us.chaoqun.ethereum.demo.service.EthereumService;

@RestController
@RequestMapping("/api")
public class EthereumController {
    @Autowired
    private EthereumService ethereumService;
    
    @PostMapping("/transaction")
    public ResponseEntity<TransactionResponse> createTransaction(@RequestBody TransactionRequest request) {
        TransactionResponse response = ethereumService.createTransaction(request);
        return ResponseEntity.ok(response);
    }
}
