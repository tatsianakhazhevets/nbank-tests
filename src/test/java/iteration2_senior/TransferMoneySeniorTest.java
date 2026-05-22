package iteration2_senior;

import iteration2_senior.assertions.AssertingClass;
import iteration2_senior.models.*;
import iteration2_senior.skelethon.endpoints.Endpoint;
import iteration2_senior.skelethon.requests.CrudRequester;
import iteration2_senior.skelethon.requests.ValidatedCrudRequester;
import iteration2_senior.specs.RequestSpecs;
import iteration2_senior.specs.ResponseSpecs;
import iteration2_senior.steps.AccountCheckStep;
import iteration2_senior.steps.AdminStep;
import iteration2_senior.steps.DepositStep;
import iteration2_senior.steps.UserStep;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static iteration2_senior.specs.Messages.*;

public class TransferMoneySeniorTest extends BaseTest {

    public static Stream<Arguments> validTransfersBetweenOwnAccounts() {
        return Stream.of(
                Arguments.of(2500, 100), // Authorized user can transfer money between their accounts (T11_Positive test)
                Arguments.of(5000, 10000), // Authorized user can transfer 10000.00 between their accounts (T12_Positive test)
                Arguments.of(5000, 9999.99), // Authorized user can transfer 9999.99 between their accounts (T13_Positive test)
                Arguments.of(5000, 0.01), // Transfer amount must be positive – test with 0,01 (T19_Positive test)
                Arguments.of(5000, 0.02), // Transfer amount must be positive – test with 0,02 (T20_Positive test)
                Arguments.of(2000, 4000), // Authorized user can transfer less than 10000.00 and equals to their balance (T23_Positive test)
                Arguments.of(2000, 3999) // Authorized first user can transfer less than 10000 and equals to their balance minus 0.01 (T26_Positive test)
        );
    }

    @MethodSource("validTransfersBetweenOwnAccounts")
    @ParameterizedTest
    public void userTransfersMoneyBetweenOwnAccountsSuccessfully(double deposit, double transferAmount) {
        CreateUserRequest user = AdminStep.createUser();
        UserStep.login(user);
        CreateUserAccountResponse firstAccountId = UserStep.createUserAccount(user);
        for (int i = 0; i < 2; i++) {
            DepositStep.depositMoney(user, firstAccountId, deposit);
        }
        CreateUserAccountResponse secondAccountId = UserStep.createUserAccount(user);

        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(firstAccountId.getId())
                .receiverAccountId(secondAccountId.getId())
                .amount(transferAmount)
                .build();

        TransferMoneyResponse transferMoneyResponse = new ValidatedCrudRequester<TransferMoneyResponse>(
                RequestSpecs.authUserSpec(user.getUsername(), user.getPassword()),
                Endpoint.TRANSFER_POST,
                ResponseSpecs.requestReturnsOk())
                .post(transferMoneyRequest);

        List<UserAccountsResponse> userAccounts = AccountCheckStep.getUserAccount(user);
        UserAccountsResponse retrieveFirstAccountsResponse = AccountCheckStep.retrieveAccount(userAccounts, firstAccountId);
        UserAccountsResponse retrieveSecondAccountsResponse = AccountCheckStep.retrieveAccount(userAccounts, secondAccountId);
        TransactionNestedResponse userTransferInAmount = AccountCheckStep.getTransferInAmount(retrieveSecondAccountsResponse);
        TransactionNestedResponse userTransferOutAmount = AccountCheckStep.getTransferOutAmount(retrieveFirstAccountsResponse);

        AssertingClass.assertThat(transferMoneyRequest, transferMoneyResponse).match();
        softly.assertThat(userTransferInAmount.getAmount()).isEqualTo(transferAmount);
        softly.assertThat(userTransferOutAmount.getAmount()).isEqualTo(transferAmount);
        softly.assertThat(retrieveFirstAccountsResponse.getBalance()).isEqualTo(((deposit + deposit) - transferAmount));
        softly.assertThat(retrieveSecondAccountsResponse.getBalance()).isEqualTo(transferAmount);
        softly.assertThat(transferMoneyResponse.getMessage()).isEqualTo(TRANSFER_SUCCESSFUL.getMessage());
    }

    public static Stream<Arguments> invalidTransfersBetweenOwnAccounts() {
        return Stream.of(
                Arguments.of(5000, 10000.01, TRANSFER_AMOUNT_CANNOT_EXCEED_10000.getMessage()), // Authorized first user can not transfer 10000.01 between their accounts (T15_Negative test)
                Arguments.of(50, 0.00, TRANSFER_AMOUNT_MUST_BE_AT_LEAST_0_01.getMessage()), // Transfer amount must be positive – test with 0.00 within first user accounts (T21_Negative test)
                Arguments.of(300, 901, INVALID_TRANSFER.getMessage()) // Authorized first user cannot transfer less than their balance (balance plus 0.01) between their accounts (T27_Negative test)
        );
    }

    @MethodSource("invalidTransfersBetweenOwnAccounts")
    @ParameterizedTest
    public void userCannotTransferInvalidAmountsBetweenAccounts(double deposit, double transferAmount, String errorMessage) {

        CreateUserRequest user = AdminStep.createUser();
        UserStep.login(user);
        CreateUserAccountResponse firstAccountId = UserStep.createUserAccount(user);
        for (int i = 0; i < 3; i++) {
            DepositStep.depositMoney(user, firstAccountId, deposit);
        }
        CreateUserAccountResponse secondAccountId = UserStep.createUserAccount(user);

        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(firstAccountId.getId())
                .receiverAccountId(secondAccountId.getId())
                .amount(transferAmount)
                .build();

        new CrudRequester(
                RequestSpecs.authUserSpec(user.getUsername(), user.getPassword()),
                Endpoint.TRANSFER_POST,
                ResponseSpecs.requestReturnsBadRequest(errorMessage))
                .post(transferMoneyRequest);

        List<UserAccountsResponse> userAccounts = AccountCheckStep.getUserAccount(user);
        UserAccountsResponse retrieveFirstAccountsResponse = AccountCheckStep.retrieveAccount(userAccounts, firstAccountId);
        UserAccountsResponse retrieveSecondAccountsResponse = AccountCheckStep.retrieveAccount(userAccounts, secondAccountId);
        int userTransferInAmount = retrieveSecondAccountsResponse.getTransactions().size();

        softly.assertThat(retrieveFirstAccountsResponse.getBalance()).isEqualTo((deposit * 3));
        softly.assertThat(retrieveSecondAccountsResponse.getBalance()).isEqualTo((0.0f));
        softly.assertThat(userTransferInAmount).isEqualTo(0);
    }


    public static Stream<Arguments> validTransferToAnotherUser() {
        return Stream.of(
                Arguments.of(2500, 100), // Authorized first user can transfer money to the authorized second user (T10_Positive test)
                Arguments.of(5000, 10000), // Authorized first user can transfer 10000.00 to authorized second user (T16_Positive test)
                Arguments.of(5000, 9999.99) // Authorized first user can transfer 9999.99 to authorized second user (T17_Positive test)
        );
    }

    @MethodSource("validTransferToAnotherUser")
    @ParameterizedTest
    public void userCanTransferMoneyToAnotherUserSuccessfully(double deposit, double transferAmount) {

        CreateUserRequest firstUser = AdminStep.createUser();
        UserStep.login(firstUser);
        CreateUserAccountResponse firstUserAccountId = UserStep.createUserAccount(firstUser);
        for (int i = 0; i < 2; i++) {
            DepositStep.depositMoney(firstUser, firstUserAccountId, deposit);
        }
        CreateUserRequest secondUser = AdminStep.createUser();
        UserStep.login(secondUser);
        CreateUserAccountResponse secondUserAccountId = UserStep.createUserAccount(secondUser);

        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(firstUserAccountId.getId())
                .receiverAccountId(secondUserAccountId.getId())
                .amount(transferAmount)
                .build();

        TransferMoneyResponse transferMoneyResponse = new ValidatedCrudRequester<TransferMoneyResponse>(
                RequestSpecs.authUserSpec(firstUser.getUsername(), secondUser.getPassword()),
                Endpoint.TRANSFER_POST,
                ResponseSpecs.requestReturnsOk())
                .post(transferMoneyRequest);


        List<UserAccountsResponse> firstUserAccounts = AccountCheckStep.getUserAccount(firstUser);
        List<UserAccountsResponse> secondUserAccounts = AccountCheckStep.getUserAccount(secondUser);
        UserAccountsResponse retrieveFirstUserAccountsResponse = AccountCheckStep.retrieveAccount(firstUserAccounts, firstUserAccountId);
        UserAccountsResponse retrieveSecondUserAccountsResponse = AccountCheckStep.retrieveAccount(secondUserAccounts, secondUserAccountId);
        TransactionNestedResponse userTransferInAmount = AccountCheckStep.getTransferInAmount(retrieveSecondUserAccountsResponse);
        TransactionNestedResponse userTransferOutAmount = AccountCheckStep.getTransferOutAmount(retrieveFirstUserAccountsResponse);

        AssertingClass.assertThat(transferMoneyRequest, transferMoneyResponse).match();
        softly.assertThat(userTransferInAmount.getAmount()).isEqualTo(transferAmount);
        softly.assertThat(userTransferOutAmount.getAmount()).isEqualTo(transferAmount);
        softly.assertThat(retrieveFirstUserAccountsResponse.getBalance()).isEqualTo(((deposit + deposit) - transferAmount));
        softly.assertThat(retrieveSecondUserAccountsResponse.getBalance()).isEqualTo(transferAmount);
        softly.assertThat(transferMoneyResponse.getMessage()).isEqualTo(TRANSFER_SUCCESSFUL.getMessage());
    }


    public static Stream<Arguments> invalidTransferToAnotherUser() {
        return Stream.of(
                Arguments.of(5000, 10000.01, TRANSFER_AMOUNT_CANNOT_EXCEED_10000.getMessage()), // Authorized first user cannot transfer 10000.01 to authorized second user (T18_Negative test)
                Arguments.of(200, 0.00, TRANSFER_AMOUNT_MUST_BE_AT_LEAST_0_01.getMessage()), // Transfer amount must be positive – test with 0.00 among accounts of the different users (T22_Negative test)
                Arguments.of(300, 900.01, INVALID_TRANSFER.getMessage()) // Authorized first user cannot transfer less than their balance (balance plus 0.1) to account of authorized second user (T28_Negative test)
        );
    }

    @MethodSource("invalidTransferToAnotherUser")
    @ParameterizedTest
    public void userCannotTransferMoneyToAnotherUser(double deposit, double transferAmount, String errorMessage) {

        CreateUserRequest firstUser = AdminStep.createUser();
        UserStep.login(firstUser);
        CreateUserAccountResponse firstUserAccountId = UserStep.createUserAccount(firstUser);
        for (int i = 0; i < 3; i++) {
            DepositStep.depositMoney(firstUser, firstUserAccountId, deposit);
        }
        CreateUserRequest secondUser = AdminStep.createUser();
        UserStep.login(secondUser);
        CreateUserAccountResponse secondUserAccountId = UserStep.createUserAccount(secondUser);

        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(firstUserAccountId.getId())
                .receiverAccountId(secondUserAccountId.getId())
                .amount(transferAmount)
                .build();

        new CrudRequester(
                RequestSpecs.authUserSpec(firstUser.getUsername(), firstUser.getPassword()),
                Endpoint.TRANSFER_POST,
                ResponseSpecs.requestReturnsBadRequest(errorMessage))
                .post(transferMoneyRequest);

        List<UserAccountsResponse> firstUserAccounts = AccountCheckStep.getUserAccount(firstUser);
        List<UserAccountsResponse> secondUserAccounts = AccountCheckStep.getUserAccount(secondUser);
        UserAccountsResponse retrieveFirstUserAccountsResponse = AccountCheckStep.retrieveAccount(firstUserAccounts, firstUserAccountId);
        UserAccountsResponse retrieveSecondUserAccountsResponse = AccountCheckStep.retrieveAccount(secondUserAccounts, secondUserAccountId);
        int secondUserTransfer = retrieveSecondUserAccountsResponse.getTransactions().size();

        softly.assertThat(retrieveFirstUserAccountsResponse.getBalance()).isEqualTo((deposit * 3));
        softly.assertThat(retrieveSecondUserAccountsResponse.getBalance()).isEqualTo((0.0f));
        softly.assertThat(secondUserTransfer).isEqualTo(0);
    }
}