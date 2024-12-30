package com.ing.loan.controller;

import com.ing.loan.entity.Loan;
import com.ing.loan.entity.LoanInstallment;
import com.ing.loan.request.LoanRequest;
import com.ing.loan.response.LoanResponse;
import com.ing.loan.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Suleyman Yildirim
 */

@RestController
@RequestMapping("/v1")
@Validated
public class LoanController {

    private LoanService loanService;

    @Autowired
    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @Operation(summary = "Create Loan", description = "Create a new loan for a given customer, amount, interest rate, and number of installments.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan created successfully",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = LoanResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @Parameters({
            @Parameter(name = "loanRequest", description = "Loan request object containing customerId, loanAmount, interestRate, and number of installments.", required = true,
                    schema = @Schema(implementation = LoanRequest.class))
    })

    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('CUSTOMER') and #loanRequest.customerId == principal.id)")
    @PostMapping("/create-loan")
    public ResponseEntity<LoanResponse> createLoan(@Valid @RequestBody LoanRequest loanRequest) {
        LoanResponse loanResponse = loanService.createLoan(loanRequest);
        return new ResponseEntity<>(loanResponse, HttpStatus.CREATED);
    }

    @Operation(summary = "List Loans", description = "List loans for a given customer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved loans",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Loan.class))}),
            @ApiResponse(responseCode = "204", description = "No loans found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @Parameter(name = "customerId", description = "ID of the customer whose loans are being retrieved", required = true, example = "1")

    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('CUSTOMER') and #customerId == principal.id)")
    @GetMapping("/loans")
    public ResponseEntity<List<Loan>> listLoans(
            @RequestParam Long customerId) {
        List<Loan> loans = loanService.listLoansByCustomer(customerId);
        if (loans.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(loans);
    }

    @Operation(summary = "List Installments", description = "List installments for a given loan.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved installments",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = LoanInstallment.class))}),
            @ApiResponse(responseCode = "204", description = "No installments found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @Parameter(name = "loanId", description = "ID of the loan whose installments are being retrieved", required = true, example = "1")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('CUSTOMER') and #loanId == principal.id)")
    @GetMapping("/installments")
    public ResponseEntity<List<LoanInstallment>> listInstallments(
            @RequestParam Long loanId) {
        List<LoanInstallment> installments = loanService.listInstallmentsByLoan(loanId);
        if (installments.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(installments);
    }

}
