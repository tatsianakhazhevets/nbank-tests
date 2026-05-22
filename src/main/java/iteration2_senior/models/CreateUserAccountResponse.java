package iteration2_senior.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserAccountResponse extends BaseModel {
    private int id;
    private String accountNumber;
    private double balance;
    private List<TransactionNestedResponse> transactions;
}