package com.ing.loan.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author Suleyman Yildirim
 */

@Getter
@Setter
@Builder
@Schema(description = "Represents the response model for a loan, including the loan ID and a list of installments.")
public class LoanResponse {

    @Schema(description = "The unique identifier for the loan", example = "12345")
    private Long loanId;

    @Schema(description = "A list of installments associated with the loan")
    private List<LoanInstallmentResponse> installments;
}