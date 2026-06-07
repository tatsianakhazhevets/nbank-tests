package iteration2_senior.steps;

import io.restassured.common.mapper.TypeRef;
import iteration2_senior.models.*;
import iteration2_senior.skelethon.endpoints.Endpoint;
import iteration2_senior.skelethon.requests.CrudRequester;
import iteration2_senior.specs.RequestSpecs;
import iteration2_senior.specs.ResponseSpecs;

import java.util.List;

public class AccountCheckStep {

    public static List<UserAccountsResponse> getUserAccount(CreateUserRequest createUserRequest) {
        return new CrudRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                Endpoint.CUSTOMER_ACCOUNTS_GET,
                ResponseSpecs.requestReturnsOk())
                .get()
                .extract().as(new TypeRef<>() {
                });
    }

    public static UserAccountsResponse retrieveAccount(List<UserAccountsResponse> userAccounts, CreateUserAccountResponse accountId) {
        return userAccounts.stream()
                .filter(acc -> acc.getId() == accountId.getId())
                .findFirst()
                .orElseThrow();
    }

    public static TransactionNestedResponse getTransferInAmount(UserAccountsResponse retrieveAccount) {
        return retrieveAccount.getTransactions().stream()
                .filter(t -> TransactionType.TRANSFER_IN.getType().equals(t.getType()))
                .findFirst()
                .orElseThrow();
    }

    public static TransactionNestedResponse getTransferOutAmount(UserAccountsResponse retrieveAccount) {
        return retrieveAccount.getTransactions().stream()
                .filter(t -> TransactionType.TRANSFER_OUT.getType().equals(t.getType()))
                .findFirst()
                .orElseThrow();
    }
}