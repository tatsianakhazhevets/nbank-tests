package apiTests.iteration2_senior.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferWithFraudCheckResponse extends BaseModel {
    private double fraudRiskScore;
    private int receiverAccountId;
    private boolean requiresVerification;
    private String message;
    private double amount;
    private String status;
    private int senderAccountId;
    private String fraudReason;
    private int transactionId;
    private boolean requiresManualReview;
}