package com.example.LoanCalculator.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class LoanResponse {

    private List<PaymentSchedule> paymentSchedules;

    @Getter
    @Setter
    public static class PaymentSchedule {
        private LocalDate paymentDate;
        private BigDecimal paymentAmount;
        private BigDecimal interestAmount;
        private BigDecimal principalAmount;
        private BigDecimal remainingLoanAmount;
    }
}
