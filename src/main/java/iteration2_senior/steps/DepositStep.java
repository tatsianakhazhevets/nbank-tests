package iteration2_senior.steps;

import iteration2_senior.models.CreateUserAccountResponse;
import iteration2_senior.models.CreateUserRequest;
import iteration2_senior.models.DepositMoneyRequest;
import iteration2_senior.models.DepositMoneyResponse;
import iteration2_senior.skelethon.endpoints.Endpoint;
import iteration2_senior.skelethon.requests.ValidatedCrudRequester;
import iteration2_senior.specs.RequestSpecs;
import iteration2_senior.specs.ResponseSpecs;

public class DepositStep {
    public static DepositMoneyResponse depositMoney(CreateUserRequest createUserRequest,
                                                    CreateUserAccountResponse accountId,
                                                    double deposit) {

        DepositMoneyRequest depositMoneyRequest = DepositMoneyRequest.builder()
                .id(accountId.getId())
                .balance(deposit)
                .build();

        return new ValidatedCrudRequester<DepositMoneyResponse>(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                Endpoint.DEPOSIT_POST,
                ResponseSpecs.requestReturnsOk())
                .post(depositMoneyRequest);
    }
}