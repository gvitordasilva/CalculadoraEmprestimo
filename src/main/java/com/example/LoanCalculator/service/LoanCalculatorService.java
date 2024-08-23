package com.example.LoanCalculator.service;

import com.example.LoanCalculator.model.LoanRequest;
import com.example.LoanCalculator.model.LoanResponse;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class LoanCalculatorService {

    public LoanResponse calculateLoan(LoanRequest request) {
        validateDates(request);

        List<LocalDate> paymentDates = generatePaymentDates(request.getStartDate(), request.getEndDate(), request.getFirstPaymentDate());
        List<PaymentDetail> paymentDetails = calculatePaymentAmounts(paymentDates.size(), request.getLoanAmount(), request.getInterestRate(), paymentDates, request);

        List<LoanResponse.PaymentSchedule> paymentSchedules = new ArrayList<>();
        for (int i = 0; i < paymentDates.size(); i++) {
            LoanResponse.PaymentSchedule schedule = new LoanResponse.PaymentSchedule();
            schedule.setPaymentDate(paymentDates.get(i));
            schedule.setPaymentAmount(paymentDetails.get(i).getPaymentAmount());
            schedule.setInterestAmount(paymentDetails.get(i).getInterestPayment());
            schedule.setPrincipalAmount(paymentDetails.get(i).getPrincipalPayment());
            schedule.setRemainingLoanAmount(paymentDetails.get(i).getRemainingLoanAmount());
            paymentSchedules.add(schedule);
        }

        LoanResponse response = new LoanResponse();
        response.setPaymentSchedules(paymentSchedules);

        return response;
    }

    private void validateDates(LoanRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date.");
        }

        if (request.getFirstPaymentDate().isBefore(request.getStartDate()) ||
                request.getFirstPaymentDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("First payment date must be after start date and before end date.");
        }
    }

    private List<LocalDate> generatePaymentDates(LocalDate startDate, LocalDate endDate, LocalDate firstPaymentDate) {
        List<LocalDate> paymentDates = new ArrayList<>();

        LocalDate currentDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        while (!currentDate.isAfter(endDate)) {
            paymentDates.add(currentDate);
            currentDate = currentDate.plusMonths(1).withDayOfMonth(currentDate.plusMonths(1).lengthOfMonth());
        }

        LocalDate paymentDate = firstPaymentDate;
        while (!paymentDate.isAfter(endDate)) {
            if (!paymentDates.contains(paymentDate)) {
                paymentDates.add(paymentDate);
            }
            paymentDate = paymentDate.plusMonths(1);
        }

        if (!paymentDates.contains(endDate)) {
            paymentDates.add(endDate);
        }

        paymentDates.sort(LocalDate::compareTo);
        return paymentDates;
    }

    private List<PaymentDetail> calculatePaymentAmounts(int totalPayments, BigDecimal loanAmount, BigDecimal annualInterestRate, List<LocalDate> paymentDates, LoanRequest request) {
        List<PaymentDetail> paymentDetails = new ArrayList<>();
        BigDecimal amortization;
        BigDecimal remainingLoanAmount = loanAmount;
        BigDecimal monthlyInterestRate = annualInterestRate.divide(BigDecimal.valueOf(12), 4, RoundingMode.HALF_UP).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        for (int i = 0; i < totalPayments; i++) {
            BigDecimal interestPayment = remainingLoanAmount.multiply(monthlyInterestRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal paymentAmount;

            if (paymentDates.get(i).getDayOfMonth() == request.getFirstPaymentDate().getDayOfMonth() ||
                    paymentDates.get(i).equals(request.getEndDate())) {
                amortization = loanAmount.divide(BigDecimal.valueOf(120), 2, RoundingMode.HALF_UP);
                paymentAmount = amortization.add(interestPayment).setScale(2, RoundingMode.HALF_UP);
            } else {
                amortization = BigDecimal.ZERO;
                paymentAmount = amortization.add(interestPayment).setScale(2, RoundingMode.HALF_UP);
            }

            remainingLoanAmount = remainingLoanAmount.subtract(amortization).setScale(2, RoundingMode.HALF_UP);

            paymentDetails.add(new PaymentDetail(amortization, interestPayment, paymentAmount, remainingLoanAmount));
        }

        return paymentDetails;
    }

    @Getter
    @Setter
    public static class PaymentDetail {
        private BigDecimal principalPayment;
        private BigDecimal interestPayment;
        private BigDecimal paymentAmount;
        private BigDecimal remainingLoanAmount;

        public PaymentDetail(BigDecimal principalPayment, BigDecimal interestPayment, BigDecimal paymentAmount, BigDecimal remainingLoanAmount) {
            this.principalPayment = principalPayment;
            this.interestPayment = interestPayment;
            this.paymentAmount = paymentAmount;
            this.remainingLoanAmount = remainingLoanAmount;
        }
    }
}