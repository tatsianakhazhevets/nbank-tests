package apiTests.iteration2_senior.specs;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Messages {
    UNAUTHORIZED_ACCESS_TO_ACCOUNT("Unauthorized access to account"),
    DEPOSIT_AMOUNT_CANNOT_EXCEED_5000("Deposit amount cannot exceed 5000"),
    DEPOSIT_AMOUNT_MUST_BE_AT_LEAST_0_01("Deposit amount must be at least 0.01"),
    TRANSFER_AMOUNT_CANNOT_EXCEED_10000("Transfer amount cannot exceed 10000"),
    TRANSFER_AMOUNT_MUST_BE_AT_LEAST_0_01("Transfer amount must be at least 0.01"),
    INVALID_TRANSFER("Invalid transfer: insufficient funds or invalid accounts"),
    NAME_MUST_CONTAINS_TWO_WORDS("Name must contain two words with letters only"),
    TRANSFER_SUCCESSFUL("Transfer successful"),
    PROFILE_UPDATED_SUCCESSFULLY("Profile updated successfully");

    private final String message;
}