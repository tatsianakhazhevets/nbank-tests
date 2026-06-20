package apiTests.iteration2_senior;

import apiTests.iteration2_senior.assertions.AssertingClass;
import apiTests.iteration2_senior.generators.RandomModelGenerator;
import apiTests.iteration2_senior.models.*;
import apiTests.iteration2_senior.skelethon.endpoints.Endpoint;
import apiTests.iteration2_senior.skelethon.requests.CrudRequester;
import apiTests.iteration2_senior.skelethon.requests.ValidatedCrudRequester;
import apiTests.iteration2_senior.specs.RequestSpecs;
import apiTests.iteration2_senior.specs.ResponseSpecs;
import apiTests.iteration2_senior.steps.AccountCheckStep;
import apiTests.iteration2_senior.steps.AdminStep;
import apiTests.iteration2_senior.steps.UserStep;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

public class DepositMoneySeniorTest extends BaseTest {

    @ValueSource(doubles = {
            2500.00, // Authorized user can deposit money (T1_Positive test)
            5000.00, // Authorized user can deposit 5000.00 (T2_Positive test)
            4999.99, // Authorized user can deposit 4999.99 (T3_Positive test)
            0.01, // Deposit amount must be positive – test with 0,01 (T5_Positive test)
            0.02 // Deposit amount must be positive – test with 0,02 (T6_Positive test)
    })
    @ParameterizedTest
    public void authorizedUserDepositsMoneySuccessfully(double deposit) {
        CreateUserRequest user = AdminStep.createUser();
        UserStep.login(user);
        CreateUserAccountResponse accountId = UserStep.createUserAccount(user);

        DepositMoneyRequest depositMoneyRequest = DepositMoneyRequest.builder()
                .id(accountId.getId())
                .balance(deposit)
                .build();

        DepositMoneyResponse depositMoneyResponse = new ValidatedCrudRequester<DepositMoneyResponse>(
                RequestSpecs.authUserSpec(user.getUsername(), user.getPassword()),
                Endpoint.DEPOSIT_POST,
                ResponseSpecs.requestReturnsOk())
                .post(depositMoneyRequest);

        AssertingClass.assertThat(depositMoneyRequest, depositMoneyResponse).match();
        softly.assertThat(depositMoneyResponse.getId()).isEqualTo(accountId.getId());
        softly.assertThat(depositMoneyResponse.getAccountNumber()).isEqualTo(accountId.getAccountNumber());
        softly.assertThat(depositMoneyResponse.getBalance()).isNotEqualTo(accountId.getBalance());
        softly.assertThat(depositMoneyResponse.getTransactions().get(0).getType()).isEqualTo(TransactionType.DEPOSIT.getType());
        softly.assertThat(depositMoneyResponse.getTransactions().size()).isEqualTo(accountId.getTransactions().size() + 1);
    }


    public static Stream<Arguments> depositInvalidCases() {
        return Stream.of(
                Arguments.of(5000.01, DEPOSIT_AMOUNT_CANNOT_EXCEED_5000.getMessage()), // Authorized user cannot deposit 5000.01 (T4_Negative)
                Arguments.of(0.00, DEPOSIT_AMOUNT_MUST_BE_AT_LEAST_0_01.getMessage()) // Deposit amount must be positive – test with 0,00 (T7_Negative)
        );
    }

    @MethodSource("depositInvalidCases")
    @ParameterizedTest
    public void authorizedUserCannotDepositInvalidAmount(double deposit, String errorMessage) {
        CreateUserRequest user = AdminStep.createUser();
        UserStep.login(user);
        CreateUserAccountResponse accountId = UserStep.createUserAccount(user);

        DepositMoneyRequest depositMoneyRequest = DepositMoneyRequest.builder()
                .id(accountId.getId())
                .balance(deposit)
                .build();

        new CrudRequester(RequestSpecs.authUserSpec(user.getUsername(), user.getPassword()),
                Endpoint.DEPOSIT_POST,
                ResponseSpecs.requestReturnsBadRequest(errorMessage))
                .post(depositMoneyRequest);

        List<UserAccountsResponse> userAccount = AccountCheckStep.getUserAccount(user);
        UserAccountsResponse userAccountsResponse = userAccount.get(0);
        int userTransaction = userAccountsResponse.getTransactions().size();

        softly.assertThat(userAccountsResponse.getBalance()).isEqualTo(0.0);
        softly.assertThat(userTransaction).isEqualTo(0);
    }


    @Test
    public void authorizedUserCannotDepositToNonExistingAccount() {
        CreateUserRequest user = AdminStep.createUser();
        UserStep.login(user);

        DepositMoneyRequest depositMoneyRequest = RandomModelGenerator.generate(DepositMoneyRequest.class);

        new CrudRequester(RequestSpecs.authUserSpec(user.getUsername(), user.getPassword()),
                Endpoint.DEPOSIT_POST,
                ResponseSpecs.requestReturnsForbidden(UNAUTHORIZED_ACCESS_TO_ACCOUNT.getMessage()))
                .post(depositMoneyRequest);

        List<UserAccountsResponse> userAccount = AccountCheckStep.getUserAccount(user);
        boolean retrieveAccountsResponse = userAccount.isEmpty();

        softly.assertThat(retrieveAccountsResponse).isTrue();
    }


    @Test
    public void authorizedUserCannotDepositToAnotherUsersAccount() {
        CreateUserRequest firstUser = AdminStep.createUser();
        UserStep.login(firstUser);
        CreateUserRequest secondUser = AdminStep.createUser();
        UserStep.login(secondUser);
        CreateUserAccountResponse secondUserAccountId = UserStep.createUserAccount(secondUser);

        DepositMoneyRequest depositMoneyRequest = RandomModelGenerator
                .generate(DepositMoneyRequest.class)
                .toBuilder()
                .id(secondUserAccountId.getId())
                .build();

        new CrudRequester(RequestSpecs.authUserSpec(firstUser.getUsername(), firstUser.getPassword()),
                Endpoint.DEPOSIT_POST,
                ResponseSpecs.requestReturnsForbidden(UNAUTHORIZED_ACCESS_TO_ACCOUNT.getMessage()))
                .post(depositMoneyRequest);

        List<UserAccountsResponse> userAccount = AccountCheckStep.getUserAccount(secondUser);
        UserAccountsResponse userAccountsResponse = userAccount.get(0);
        int userTransaction = userAccountsResponse.getTransactions().size();

        softly.assertThat(userAccountsResponse.getBalance()).isEqualTo(0.0);
        softly.assertThat(userTransaction).isEqualTo(0);
    }
}