package com.ing.loan.controller;

import com.ing.loan.exception.LoanNotFoundException;
import com.ing.loan.request.PaymentRequest;
import com.ing.loan.response.LoanPaymentResponse;
import com.ing.loan.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Suleyman Yildirim
 */

@RestController
@RequestMapping("/v1")
@Validated
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(summary = "Pay Loan", description = "Process a payment for a given loan.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment processed successfully",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = LoanPaymentResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Loan not found",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @Parameter(name = "paymentRequest", description = "Request object containing loanId and paymentAmount.", required = true,
            schema = @Schema(implementation = PaymentRequest.class))

    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('CUSTOMER') and #paymentRequest.loanId == principal.id)")
    @PostMapping("/pay-loan")
    public ResponseEntity<LoanPaymentResponse> payLoan(@Valid @RequestBody PaymentRequest paymentRequest) {
        try {
            LoanPaymentResponse loanResponse = paymentService.payLoan(paymentRequest.getLoanId(), paymentRequest.getPaymentAmount());
            return ResponseEntity.ok(loanResponse);
        } catch (LoanNotFoundException e) {
            // Handle specific exception with an appropriate status code (e.g., 404 Not Found)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            // Handle unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
