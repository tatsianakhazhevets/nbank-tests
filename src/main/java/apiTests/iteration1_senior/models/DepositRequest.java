package apiTests.iteration1_senior.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepositRequest extends BaseModel {
    private Long accountId;
    private double amount;
    private String description;
}