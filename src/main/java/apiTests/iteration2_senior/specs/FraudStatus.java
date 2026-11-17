package apiTests.iteration2_senior.specs;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FraudStatus {
    APPROVED("APPROVED"),
    BLOCKED("BLOCKED"),
    MANUAL_REVIEW_REQUIRED("MANUAL_REVIEW_REQUIRED"),
    VERIFICATION_REQUIRED("VERIFICATION_REQUIRED"),
    NO_FRAUD_CHECK_REQUIRED("NO_FRAUD_CHECK_REQUIRED");

    private final String status;
}