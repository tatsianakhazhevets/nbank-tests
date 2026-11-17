package apiTests.iteration2_senior.specs;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FraudMessages {
    TRANSFER_APPROVED_AND_PROCESSED_IMMEDIATELY("Transfer approved and processed immediately"),
    LOW_RISK_TRANSACTION("Low risk transaction"),
    TRANSFER_BLOCKED_DUE_TO_FRAUD_DETECTION("Transfer blocked due to fraud detection"),
    HIGH_RISK_TRANSACTION("High risk transaction"),
    TRANSFER_REQUIRED_MANUAL_REVIEW("Transfer requires manual review"),
    ADDITIONAL_VERIFICATION_REQUIRED("Additional verification required"),
    MIDDLE_RISK_TRANSACTION("Middle risk transaction"),
    UNEXPECTED_ERROR_DURING_FRAUD_CHECK_503("Unexpected error during fraud check: 503 Service Unavailable: \"{\"status\":\"SUCCESS\",\"decision\":\"APPROVE\",\"riskScore\":0.5,\"reason\":\"Middle risk transaction\",\"requiresManualReview\":true,\"additionalVerificationRequired\":false}\""),
    FRAUD_DETECTION_SERVICE_IS_CURRENTLY_UNAVAILABLE("Fraud detection service is currently unavailable");

    private final String message;
}