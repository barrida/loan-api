package com.ing.loan.service;

import com.ing.loan.entity.Customer;

import java.util.Optional;

public interface CustomerService {

    // Method to create a new customer
    Customer createCustomer(Customer customer);

    // Method to find customer by ID
    Optional<Customer> findCustomerById(Long id);
}
