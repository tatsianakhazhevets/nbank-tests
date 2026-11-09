package apiTests.iteration2_senior.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FraudCheckMockResponse extends BaseModel {
    private String status;
    private String decision;
    private double riskScore;
    private String reason;
    private boolean requiresManualReview;
    private boolean additionalVerificationRequired;
}