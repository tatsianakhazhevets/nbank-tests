package iteration2_senior.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionNestedResponse {
    private int id;
    private double amount;
    private String type;
    private String timestamp;
    private int relatedAccountId;
}