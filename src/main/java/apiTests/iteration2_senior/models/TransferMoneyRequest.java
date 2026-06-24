package apiTests.iteration2_senior.models;

import apiTests.iteration2_senior.generators.DoubleGeneratingRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferMoneyRequest extends BaseModel {
    private int senderAccountId;
    private int receiverAccountId;
    @DoubleGeneratingRule(min = 0.01, max = 10000.00, range = 2)
    private double amount;
}