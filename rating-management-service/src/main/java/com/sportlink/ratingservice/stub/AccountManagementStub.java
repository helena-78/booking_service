package com.sportlink.ratingservice.stub;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class AccountManagementStub {

    /**
     * Validates that a user exists in Account Management Service.
     * STUB: always returns true. Replace with RestTemplate call later.
     */
    public boolean userExists(UUID userId) {
        return true;
    }
}
