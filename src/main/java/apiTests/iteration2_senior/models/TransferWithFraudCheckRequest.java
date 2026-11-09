package apiTests.iteration2_senior.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferWithFraudCheckRequest extends BaseModel {
    private int senderAccountId;
    private int receiverAccountId;
    private double amount;
    private String description;
}