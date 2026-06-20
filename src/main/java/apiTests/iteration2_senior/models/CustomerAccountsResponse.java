package apiTests.iteration2_senior.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerAccountsResponse extends BaseModel {
    private List<AccountsNestedResponse> accounts;
}