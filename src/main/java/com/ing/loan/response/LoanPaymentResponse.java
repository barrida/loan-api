package com.ing.loan.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @author Suleyman Yildirim
 */

@Getter
@Setter
@Builder
@Schema(description = "Represents the response model for loan payment details, including paid installments, total amount spent, and loan status.")
public class LoanPaymentResponse {

    @Schema(description = "The number of installments that have been paid", example = "5")
    private int paidInstallments;

    @Schema(description = "The total amount spent by the customer on the loan so far", example = "2500.00")
    private BigDecimal totalAmountSpent;

    @Schema(description = "Indicates whether the loan has been fully paid off", example = "false")
    private boolean isLoanPaid;

    @Schema(description = "Error message, if any, associated with the payment attempt", example = "No valid installments found for loan 1")
    private String errorMessage;
}