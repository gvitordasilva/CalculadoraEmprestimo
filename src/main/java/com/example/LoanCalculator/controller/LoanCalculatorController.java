package com.example.LoanCalculator.controller;

import com.example.LoanCalculator.model.LoanRequest;
import com.example.LoanCalculator.model.LoanResponse;
import com.example.LoanCalculator.service.LoanCalculatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loan")
public class LoanCalculatorController {

    @Autowired
    private LoanCalculatorService loanCalculatorService;

    @PostMapping("/calculate")
    public ResponseEntity<LoanResponse> calculateLoan(@Validated @RequestBody LoanRequest request) {
        LoanResponse response = loanCalculatorService.calculateLoan(request);
        return ResponseEntity.ok(response);
    }
}
