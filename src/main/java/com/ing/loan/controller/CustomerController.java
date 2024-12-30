package com.ing.loan.controller;

import com.ing.loan.entity.Customer;
import com.ing.loan.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author Suleyman Yildirim
 */
@RestController
@RequestMapping("/v1")
@Validated
@PreAuthorize("hasAuthority('ADMIN')")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    // Endpoint to create a new customer
    @PostMapping("/create-customer")
    @Operation(
            summary = "Create a new customer",
            security = @SecurityRequirement(name = "bearerAuth"), // Security for Swagger UI
            description = "This endpoint creates a new customer and stores their information in the database."
    )
    @Parameter(name = "customer", description = "The customer object containing all necessary details for the new customer", required = true)
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        Customer savedCustomer = customerService.createCustomer(customer);
        return new ResponseEntity<>(savedCustomer, HttpStatus.CREATED);
    }

    // Endpoint to get a customer by ID
    @GetMapping("/customer/{id}")
    @Operation(
            summary = "Get customer details by ID",
            description = "This endpoint fetches the customer details for the given customer ID."
    )
    @Parameter(name = "id", description = "The unique identifier for the customer", required = true, example = "12345")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        return customerService.findCustomerById(id)
                .map(customer -> new ResponseEntity<>(customer, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
