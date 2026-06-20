package apiTests.iteration2_senior.steps;

import apiTests.iteration2_senior.models.CreateUserAccountResponse;
import apiTests.iteration2_senior.models.CreateUserRequest;
import apiTests.iteration2_senior.models.DepositMoneyRequest;
import apiTests.iteration2_senior.models.DepositMoneyResponse;
import apiTests.iteration2_senior.skelethon.endpoints.Endpoint;
import apiTests.iteration2_senior.skelethon.requests.ValidatedCrudRequester;
import apiTests.iteration2_senior.specs.RequestSpecs;
import apiTests.iteration2_senior.specs.ResponseSpecs;

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