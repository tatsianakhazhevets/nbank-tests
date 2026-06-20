package apiTests.iteration2_middle;

import apiTests.iteration2_middle.models.*;
import apiTests.iteration2_middle.requests.*;
import io.restassured.common.mapper.TypeRef;
import apiTests.iteration2_middle.specs.RequestSpecs;
import apiTests.iteration2_middle.specs.ResponseSpecs;
import apiTests.iteration2_middle.utils.RandomDataGenerator;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

public class DepositMoneyMiddleTest extends BaseTest {

    @ValueSource(doubles = {
            // Authorized user can deposit money (T1_Positive test)
            2500.00,
            // Authorized user can deposit 5000.00 (T2_Positive test)
            5000.00,
            // Authorized user can deposit 4999.99 (T3_Positive test)
            4999.99,
            // Deposit amount must be positive – test with 0,01 (T5_Positive test)
            0.01,
            // Deposit amount must be positive – test with 0,02 (T6_Positive test)
            0.02})
    @ParameterizedTest
    public void authorizedUserDepositsMoneySuccessfully(double deposit) {

        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomDataGenerator.getUsername())
                .password(RandomDataGenerator.getPassword())
                .role(Role.USER.toString())
                .build();

        new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsCreated())
                .post(createUserRequest);

        new LoginUserRequester(RequestSpecs.unAuthSpec(),
                ResponseSpecs.requestReturnsOk())
                .post(LoginUserRequest.builder()
                        .username(createUserRequest.getUsername())
                        .password(createUserRequest.getPassword())
                        .build())
                .header(RequestSpecs.AUTHORIZATION_HEADER, Matchers.notNullValue());

        CreateAccountResponse accountId = new CreateAccountRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsCreated())
                .post()
                .extract()
                .as(CreateAccountResponse.class);

        DepositMoneyRequest depositMoneyRequest = DepositMoneyRequest.builder()
                .id(accountId.getId())
                .balance(deposit)
                .build();

        new DepositMoneyRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(depositMoneyRequest);

        List<UserAccountsResponse> userAccount = new RetrieveUserAccountRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .get()
                .extract().as(new TypeRef<>() {
                });

        UserAccountsResponse userAccountsResponse = userAccount.get(0);

        TransactionBody userDepositAmount = userAccountsResponse.getTransactions().stream()
                .filter(t -> TransactionType.DEPOSIT.getType().equals(t.getType()))
                .findFirst()
                .orElseThrow();

        softly.assertThat(userAccountsResponse.getBalance()).isEqualTo(deposit);
        softly.assertThat(userDepositAmount.getAmount()).isEqualTo(deposit);
    }


    public static Stream<Arguments> depositInvalidCases() {
        return Stream.of(
                // Authorized user cannot deposit 5000.01 (T4_Negative)
                Arguments.of(5000.01, ResponseSpecs.DEPOSIT_AMOUNT_CANNOT_EXCEED_5000),
                // Deposit amount must be positive – test with 0,00 (T7_Negative)
                Arguments.of(0.00, ResponseSpecs.DEPOSIT_AMOUNT_MUST_BE_AT_LEAST_0_01)
        );
    }

    @MethodSource("depositInvalidCases")
    @ParameterizedTest
    public void authorizedUserCannotDepositInvalidAmount(double deposit, String errorMessage) {

        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomDataGenerator.getUsername())
                .password(RandomDataGenerator.getPassword())
                .role(Role.USER.toString())
                .build();

        new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsCreated())
                .post(createUserRequest);

        new LoginUserRequester(RequestSpecs.unAuthSpec(),
                ResponseSpecs.requestReturnsOk())
                .post(LoginUserRequest.builder()
                        .username(createUserRequest.getUsername())
                        .password(createUserRequest.getPassword())
                        .build())
                .header(RequestSpecs.AUTHORIZATION_HEADER, Matchers.notNullValue());

        CreateAccountResponse accountId = new CreateAccountRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsCreated())
                .post()
                .extract()
                .as(CreateAccountResponse.class);

        DepositMoneyRequest depositMoneyRequest = DepositMoneyRequest.builder()
                .id(accountId.getId())
                .balance(deposit)
                .build();

        new DepositMoneyRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest(errorMessage))
                .post(depositMoneyRequest);

        List<UserAccountsResponse> userAccount = new RetrieveUserAccountRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .get()
                .extract().as(new TypeRef<>() {
                });

        UserAccountsResponse userAccountsResponse = userAccount.get(0);

        int userTransaction = userAccountsResponse.getTransactions().size();

        softly.assertThat(userAccountsResponse.getBalance()).isEqualTo(0.0);
        softly.assertThat(userTransaction).isEqualTo(0);
    }


    @Test
    public void authorizedUserCannotDepositToNonExistingAccount() {

        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomDataGenerator.getUsername())
                .password(RandomDataGenerator.getPassword())
                .role(Role.USER.toString())
                .build();

        new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsCreated())
                .post(createUserRequest);

        new LoginUserRequester(RequestSpecs.unAuthSpec(),
                ResponseSpecs.requestReturnsOk())
                .post(LoginUserRequest.builder()
                        .username(createUserRequest.getUsername())
                        .password(createUserRequest.getPassword())
                        .build())
                .header(RequestSpecs.AUTHORIZATION_HEADER, Matchers.notNullValue());

        DepositMoneyRequest depositMoneyRequest = DepositMoneyRequest.builder()
                .id(0)
                .balance(RandomDataGenerator.getDeposit())
                .build();

        new DepositMoneyRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsForbidden(ResponseSpecs.UNAUTHORIZED_ACCESS_TO_ACCOUNT))
                .post(depositMoneyRequest);

        List<UserAccountsResponse> userAccount = new RetrieveUserAccountRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .get()
                .extract().as(new TypeRef<>() {
                });

        boolean retrieveAccountsResponse = userAccount.isEmpty();

        softly.assertThat(retrieveAccountsResponse).isTrue();
    }


    @Test
    public void authorizedUserCannotDepositToAnotherUsersAccount() {

        CreateUserRequest createFirstUserRequest = CreateUserRequest.builder()
                .username(RandomDataGenerator.getUsername())
                .password(RandomDataGenerator.getPassword())
                .role(Role.USER.toString())
                .build();

        new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsCreated())
                .post(createFirstUserRequest);

        new LoginUserRequester(RequestSpecs.unAuthSpec(),
                ResponseSpecs.requestReturnsOk())
                .post(LoginUserRequest.builder()
                        .username(createFirstUserRequest.getUsername())
                        .password(createFirstUserRequest.getPassword())
                        .build())
                .header(RequestSpecs.AUTHORIZATION_HEADER, Matchers.notNullValue());

        CreateUserRequest createSecondUserRequest = CreateUserRequest.builder()
                .username(RandomDataGenerator.getUsername())
                .password(RandomDataGenerator.getPassword())
                .role(Role.USER.toString())
                .build();

        new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsCreated())
                .post(createSecondUserRequest);

        new LoginUserRequester(RequestSpecs.unAuthSpec(),
                ResponseSpecs.requestReturnsOk())
                .post(LoginUserRequest.builder()
                        .username(createSecondUserRequest.getUsername())
                        .password(createSecondUserRequest.getPassword())
                        .build())
                .header(RequestSpecs.AUTHORIZATION_HEADER, Matchers.notNullValue());

        CreateAccountResponse secondUserAccountId = new CreateAccountRequester(
                RequestSpecs.authUserSpec(createSecondUserRequest.getUsername(), createSecondUserRequest.getPassword()),
                ResponseSpecs.requestReturnsCreated())
                .post()
                .extract()
                .as(CreateAccountResponse.class);

        DepositMoneyRequest depositMoneyRequest = DepositMoneyRequest.builder()
                .id(secondUserAccountId.getId())
                .balance(RandomDataGenerator.getDeposit())
                .build();

        new DepositMoneyRequester(
                RequestSpecs.authUserSpec(createFirstUserRequest.getUsername(), createFirstUserRequest.getPassword()),
                ResponseSpecs.requestReturnsForbidden(ResponseSpecs.UNAUTHORIZED_ACCESS_TO_ACCOUNT))
                .post(depositMoneyRequest);

        List<UserAccountsResponse> userAccount = new RetrieveUserAccountRequester(
                RequestSpecs.authUserSpec(createSecondUserRequest.getUsername(), createSecondUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .get()
                .extract().as(new TypeRef<>() {
                });

        UserAccountsResponse userAccountsResponse = userAccount.get(0);

        int userTransaction = userAccountsResponse.getTransactions().size();

        softly.assertThat(userAccountsResponse.getBalance()).isEqualTo(0.0);
        softly.assertThat(userTransaction).isEqualTo(0);
    }
}