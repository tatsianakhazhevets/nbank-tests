package apiTests.iteration2_senior.steps;

import apiTests.iteration2_senior.models.*;
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

    public static DepositMoneyFraudCheckVersionResponse depositMoneyWithFraudCheckVersion(CreateUserRequest createUserRequest,
                                                    CreateUserAccountResponse accountId, double deposit) {

        DepositMoneyFraudCheckVersionRequest depositMoneyRequest = DepositMoneyFraudCheckVersionRequest.builder()
                .accountId(accountId.getId())
                .amount(deposit)
                .build();

        return new ValidatedCrudRequester<DepositMoneyFraudCheckVersionResponse>(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                Endpoint.DEPOSIT_FRAUD_CHECK_VERSION_POST,
                ResponseSpecs.requestReturnsOk())
                .post(depositMoneyRequest);
    }
}