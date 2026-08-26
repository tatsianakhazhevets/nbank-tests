package apiTests.iteration2_senior.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionNestedResponse {
    private int id;
    private double amount;
    private String type;
    private String timestamp;
    private int relatedAccountId;
}