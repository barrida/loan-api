package com.ing.loan.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Suleyman Yildirim
 */

@Entity
@Table(name = "customer")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents a customer who can have multiple loans with associated credit information.")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Schema(description = "The unique identifier for the customer", example = "12345")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "The first name of the customer", example = "John")
    private String name;

    @Column(nullable = false)
    @Schema(description = "The last name of the customer", example = "Doe")
    private String surname;

    @Column(nullable = false)
    @Schema(description = "The credit limit available to the customer", example = "50000.00")
    private BigDecimal creditLimit;

    @Column(nullable = false)
    @Schema(description = "The credit limit used by the customer", example = "20000.00")
    private BigDecimal usedCreditLimit;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference // Prevents recursion and allows loans to be serialized
    @Schema(description = "The list of loans associated with the customer")
    private List<Loan> loans;

}