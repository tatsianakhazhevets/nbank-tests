package iteration2_senior.skelethon.endpoints;

import iteration2_senior.models.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Endpoint {

    ADMIN_USERS_POST(
            "/admin/users",
            CreateUserRequest.class,
            CreateUserResponse.class),

    LOGIN_POST(
            "/auth/login",
            LoginUserRequest.class,
            LoginUserResponse.class),

    ACCOUNTS_POST(
            "/accounts",
            BaseModel.class,
            CreateUserAccountResponse.class),

    TRANSFER_POST(
            "/accounts/transfer",
            TransferMoneyRequest.class,
            TransferMoneyResponse.class),

    DEPOSIT_POST(
            "/accounts/deposit",
            DepositMoneyRequest.class,
            DepositMoneyResponse.class),

    CUSTOMER_PROFILE_GET(
            "/customer/profile",
            BaseModel.class,
            UserProfileNestedResponse.class),

    CUSTOMER_PROFILE_UPDATE(
            "/customer/profile",
            ChangeUserNameRequest.class,
            ChangeUserNameResponse.class),

    CUSTOMER_ACCOUNTS_GET(
            "/customer/accounts",
            BaseModel.class,
            CustomerAccountsResponse.class),

    ADMIN_USERS_GET(
            "/admin/users",
            BaseModel.class,
            BaseModel.class),

    ADMIN_USERS_DELETE(
            "/admin/users",
            BaseModel.class,
            BaseModel.class);

    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}