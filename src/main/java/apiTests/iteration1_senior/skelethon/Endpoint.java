package apiTests.iteration1_senior.skelethon;

import apiTests.iteration1_senior.models.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Endpoint {

    ADMIN_USER(
            "/admin/users",
            CreateUserRequest.class,
            CreateUserResponse.class),


    LOGIN(
            "/auth/login",
            LoginUserRequest.class,
            LoginUserResponse.class
    ),

    ACCOUNTS(
            "/accounts",
            BaseModel.class,
            CreateAccountResponse.class
    ),

    CUSTOMER_ACCOUNTS(
            "/customer/accounts",
            BaseModel.class,
            CreateAccountResponse.class
    ),

    ACCOUNT_DEPOSIT(
            "/accounts/deposit",
            DepositRequest.class,
            DepositResponse.class
    ),

    ACCOUNT_TRANSFER(
            "/accounts/transfer",
            TransferRequest.class,
            TransferResponse.class
    ),

    TRANSFER_WITH_FRAUD_CHECK(
            "/accounts/transfer-with-fraud-check", //Fraud check service
            TransferRequest.class,
            TransferResponse.class
    ),

    FRAUD_CHECK_STATUS(
            "/api/v1/accounts/fraud-check/{transactionId}",
            BaseModel.class,
            FraudCheckResponse.class
    );

    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}