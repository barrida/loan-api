package com.ing.loan.service;

import com.ing.loan.entity.Customer;
import com.ing.loan.exception.CustomerExistsException;
import com.ing.loan.exception.ErrorCode;
import com.ing.loan.repository.CustomerRepository;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    @Transactional
    public Customer createCustomer(Customer customer) {

        var optionalCustomer =  customerRepository.findById(customer.getId());
        if (optionalCustomer.isEmpty()) {
            return customerRepository.save(customer);
        } else {
            throw new CustomerExistsException(ErrorCode.CUSTOMER_EXISTS,
                    "Customer with ID %s already exists.".formatted(customer.getId()));
        }
    }

    @Override
    public Optional<Customer> findCustomerById(Long id) {
        return customerRepository.findById(id);
    }
}
