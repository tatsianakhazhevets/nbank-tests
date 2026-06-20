package apiTests.iteration2_senior.models;

import apiTests.iteration2_senior.generators.DoubleGeneratingRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class DepositMoneyRequest extends BaseModel {
    private int id;
    @DoubleGeneratingRule(min = 0.01, max = 5000.00, range = 2)
    private double balance;
}