package iteration2_middle;

import io.restassured.common.mapper.TypeRef;
import iteration1.requests.CreateAccountRequester;
import iteration2_middle.models.*;
import iteration2_middle.requests.*;
import iteration2_middle.specs.RequestSpecs;
import iteration2_middle.specs.ResponseSpecs;
import iteration2_middle.utils.RandomDataGenerator;
import org.hamcrest.Matchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

public class TransferMoneyMiddleTest extends BaseTest {

    public static Stream<Arguments> validTransfersBetweenOwnAccounts() {
        return Stream.of(
                // Authorized user can transfer money between their accounts (T11_Positive test)
                Arguments.of(2500, 100.),
                // Authorized user can transfer 10000.00 between their accounts (T12_Positive test)
                Arguments.of(5000, 10000),
                // Authorized user can transfer 9999.99 between their accounts (T13_Positive test)
                Arguments.of(5000, 9999.99),
                // Transfer amount must be positive – test with 0,01 (T19_Positive test)
                Arguments.of(5000, 0.01),
                // Transfer amount must be positive – test with 0,02 (T20_Positive test)
                Arguments.of(5000, 0.02),
                // Authorized user can transfer less than 10000.00 and equals to their balance (T23_Positive test)
                Arguments.of(2000, 4000),
                // Authorized first user can transfer less than 10000 and equals to their balance minus 0.01 (T26_Positive test)
                Arguments.of(2000, 3999)
        );
    }

    @MethodSource("validTransfersBetweenOwnAccounts")
    @ParameterizedTest
    public void userTransfersMoneyBetweenOwnAccountsSuccessfully(double deposit, double transferAmount) {
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
                .header("Authorization", Matchers.notNullValue());

        int firstAccountId = new CreateAccountRequester(RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsCreated())
                .post(null)
                .extract()
                .path("id");

        DepositMoneyRequest depositMoneyRequest = DepositMoneyRequest.builder()
                .id(firstAccountId)
                .balance(deposit)
                .build();

        new DepositMoneyRequester(RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(depositMoneyRequest);

        new DepositMoneyRequester(RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(depositMoneyRequest);

        int secondAccountId = new CreateAccountRequester(RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsCreated())
                .post(null)
                .extract()
                .path("id");

        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(firstAccountId)
                .receiverAccountId(secondAccountId)
                .amount(transferAmount)
                .build();

        new TransferMoneyRequester(RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(transferMoneyRequest);

        List<UserAccountsResponse> userAccounts = new RetrieveUserAccountRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .get(null)
                .extract().as(new TypeRef<>() {
                });

        UserAccountsResponse retrieveFirstAccountsResponse = userAccounts.stream()
                .filter(acc -> acc.getId() == firstAccountId)
                .findFirst()
                .orElseThrow();

        UserAccountsResponse retrieveSecondAccountsResponse = userAccounts.stream()
                .filter(acc -> acc.getId() == secondAccountId)
                .findFirst()
                .orElseThrow();

        TransactionBody userTransferInAmount = retrieveSecondAccountsResponse.getTransactions().stream()
                .filter(t -> "TRANSFER_IN".equals(t.getType()))
                .findFirst()
                .orElseThrow();

        TransactionBody userTransferOutAmount = retrieveFirstAccountsResponse.getTransactions().stream()
                .filter(t -> "TRANSFER_OUT".equals(t.getType()))
                .findFirst()
                .orElseThrow();

        softly.assertThat(userTransferInAmount.getAmount()).isEqualTo(transferAmount);
        softly.assertThat(userTransferOutAmount.getAmount()).isEqualTo(transferAmount);
        softly.assertThat(retrieveFirstAccountsResponse.getBalance()).isEqualTo(((deposit + deposit) - transferAmount));
        softly.assertThat(retrieveSecondAccountsResponse.getBalance()).isEqualTo(transferAmount);

    }


    public static Stream<Arguments> invalidTransfersBetweenOwnAccounts() {
        return Stream.of(
                // Authorized first user can not transfer 10000.01 between their accounts (T15_Negative test)
                Arguments.of(5000, 10000.01, "Transfer amount cannot exceed 10000"),
                // Transfer amount must be positive – test with 0.00 within first user accounts (T21_Negative test)
                Arguments.of(50, 0.00, "Transfer amount must be at least 0.01"),
                // Authorized first user cannot transfer less than their balance (balance plus 0.01) between their accounts (T27_Negative test)
                Arguments.of(300, 901, "Invalid transfer: insufficient funds or invalid accounts")
        );
    }

    @MethodSource("invalidTransfersBetweenOwnAccounts")
    @ParameterizedTest
    public void userCannotTransferInvalidAmountsBetweenAccounts(double deposit, double transferAmount, String errorMessage) {

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
                .header("Authorization", Matchers.notNullValue());


        int firstAccountId = new CreateAccountRequester(RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsCreated())
                .post(null)
                .extract()
                .path("id");

        DepositMoneyRequest depositMoneyRequest = DepositMoneyRequest.builder()
                .id(firstAccountId)
                .balance(deposit)
                .build();

        new DepositMoneyRequester(RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(depositMoneyRequest);

        new DepositMoneyRequester(RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(depositMoneyRequest);

        new DepositMoneyRequester(RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(depositMoneyRequest);

        int secondAccountId = new CreateAccountRequester(RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsCreated())
                .post(null)
                .extract()
                .path("id");

        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(firstAccountId)
                .receiverAccountId(secondAccountId)
                .amount(transferAmount)
                .build();

        new TransferMoneyRequester(RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest(errorMessage))
                .post(transferMoneyRequest);

        List<UserAccountsResponse> userAccounts = new RetrieveUserAccountRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .get(null)
                .extract().as(new TypeRef<>() {
                });

        UserAccountsResponse retrieveFirstAccountsResponse = userAccounts.stream()
                .filter(acc -> acc.getId() == firstAccountId)
                .findFirst()
                .orElseThrow();

        UserAccountsResponse retrieveSecondAccountsResponse = userAccounts.stream()
                .filter(acc -> acc.getId() == secondAccountId)
                .findFirst()
                .orElseThrow();

        int userTransferInAmount = retrieveSecondAccountsResponse.getTransactions().size();

        softly.assertThat(retrieveFirstAccountsResponse.getBalance()).isEqualTo((deposit * 3));
        softly.assertThat(retrieveSecondAccountsResponse.getBalance()).isEqualTo((0.0f));
        softly.assertThat(userTransferInAmount).isEqualTo(0);
    }


    public static Stream<Arguments> validTransferToAnotherUser() {
        return Stream.of(
                // Authorized first user can transfer money to the authorized second user (T10_Positive test)
                Arguments.of(2500, 100),
                // Authorized first user can transfer 10000.00 to authorized second user (T16_Positive test)
                Arguments.of(5000, 10000),
                // Authorized first user can transfer 9999.99 to authorized second user (T17_Positive test)
                Arguments.of(5000, 9999.99)
        );
    }

    @MethodSource("validTransferToAnotherUser")
    @ParameterizedTest
    public void userCanTransferMoneyToAnotherUserSuccessfully(double deposit, double transferAmount) {

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
                .header("Authorization", Matchers.notNullValue());

        int firstUserAccountId = new CreateAccountRequester(RequestSpecs.authUserSpec(createFirstUserRequest.getUsername(), createFirstUserRequest.getPassword()),
                ResponseSpecs.requestReturnsCreated())
                .post(null)
                .extract()
                .path("id");

        DepositMoneyRequest depositMoneyRequest = DepositMoneyRequest.builder()
                .id(firstUserAccountId)
                .balance(deposit)
                .build();

        new DepositMoneyRequester(RequestSpecs.authUserSpec(createFirstUserRequest.getUsername(), createFirstUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(depositMoneyRequest);

        new DepositMoneyRequester(RequestSpecs.authUserSpec(createFirstUserRequest.getUsername(), createFirstUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(depositMoneyRequest);

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
                .header("Authorization", Matchers.notNullValue());


        int secondUserAccountId = new CreateAccountRequester(RequestSpecs.authUserSpec(createSecondUserRequest.getUsername(), createSecondUserRequest.getPassword()),
                ResponseSpecs.requestReturnsCreated())
                .post(null)
                .extract()
                .path("id");

        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(firstUserAccountId)
                .receiverAccountId(secondUserAccountId)
                .amount(transferAmount)
                .build();

        new TransferMoneyRequester(RequestSpecs.authUserSpec(createFirstUserRequest.getUsername(), createFirstUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(transferMoneyRequest);

        List<UserAccountsResponse> firstUserAccounts = new RetrieveUserAccountRequester(
                RequestSpecs.authUserSpec(createFirstUserRequest.getUsername(), createFirstUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .get(null)
                .extract().as(new TypeRef<>() {
                });

        List<UserAccountsResponse> secondUserAccounts = new RetrieveUserAccountRequester(
                RequestSpecs.authUserSpec(createSecondUserRequest.getUsername(), createSecondUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .get(null)
                .extract().as(new TypeRef<>() {
                });

        UserAccountsResponse retrieveFirstUserAccountsResponse = firstUserAccounts.stream()
                .filter(acc -> acc.getId() == firstUserAccountId)
                .findFirst()
                .orElseThrow();

        UserAccountsResponse retrieveSecondUserAccountsResponse = secondUserAccounts.stream()
                .filter(acc -> acc.getId() == secondUserAccountId)
                .findFirst()
                .orElseThrow();

        TransactionBody userTransferInAmount = retrieveSecondUserAccountsResponse.getTransactions().stream()
                .filter(t -> "TRANSFER_IN".equals(t.getType()))
                .findFirst()
                .orElseThrow();

        TransactionBody userTransferOutAmount = retrieveFirstUserAccountsResponse.getTransactions().stream()
                .filter(t -> "TRANSFER_OUT".equals(t.getType()))
                .findFirst()
                .orElseThrow();


        softly.assertThat(userTransferInAmount.getAmount()).isEqualTo(transferAmount);
        softly.assertThat(userTransferOutAmount.getAmount()).isEqualTo(transferAmount);
        softly.assertThat(retrieveFirstUserAccountsResponse.getBalance()).isEqualTo(((deposit + deposit) - transferAmount));
        softly.assertThat(retrieveSecondUserAccountsResponse.getBalance()).isEqualTo(transferAmount);
    }


    public static Stream<Arguments> invalidTransferToAnotherUser() {
        return Stream.of(
                // Authorized first user cannot transfer 10000.01 to authorized second user (T18_Negative test)
                Arguments.of(5000, 10000.01, "Transfer amount cannot exceed 10000"),
                // Transfer amount must be positive – test with 0.00 among accounts of the different users (T22_Negative test)
                Arguments.of(200, 0.00, "Transfer amount must be at least 0.01"),
                // Authorized first user cannot transfer less than their balance (balance plus 0.1) to account of authorized second user (T28_Negative test)
                Arguments.of(300, 900.01, "Invalid transfer: insufficient funds or invalid accounts")
        );
    }

    @MethodSource("invalidTransferToAnotherUser")
    @ParameterizedTest
    public void userCannotTransferMoneyToAnotherUser(double deposit, double transferAmount, String errorMessage) {

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
                .header("Authorization", Matchers.notNullValue());


        int firstUserAccountId = new CreateAccountRequester(RequestSpecs.authUserSpec(createFirstUserRequest.getUsername(), createFirstUserRequest.getPassword()),
                ResponseSpecs.requestReturnsCreated())
                .post(null)
                .extract()
                .path("id");

        DepositMoneyRequest depositMoneyRequest = DepositMoneyRequest.builder()
                .id(firstUserAccountId)
                .balance(deposit)
                .build();

        new DepositMoneyRequester(RequestSpecs.authUserSpec(createFirstUserRequest.getUsername(), createFirstUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(depositMoneyRequest);

        new DepositMoneyRequester(RequestSpecs.authUserSpec(createFirstUserRequest.getUsername(), createFirstUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(depositMoneyRequest);

        new DepositMoneyRequester(RequestSpecs.authUserSpec(createFirstUserRequest.getUsername(), createFirstUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(depositMoneyRequest);

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
                .header("Authorization", Matchers.notNullValue());

        int secondUserAccountId = new CreateAccountRequester(RequestSpecs.authUserSpec(createSecondUserRequest.getUsername(), createSecondUserRequest.getPassword()),
                ResponseSpecs.requestReturnsCreated())
                .post(null)
                .extract()
                .path("id");

        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(firstUserAccountId)
                .receiverAccountId(secondUserAccountId)
                .amount(transferAmount)
                .build();

        new TransferMoneyRequester(RequestSpecs.authUserSpec(createFirstUserRequest.getUsername(), createFirstUserRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest(errorMessage))
                .post(transferMoneyRequest);


        List<UserAccountsResponse> firstUserAccounts = new RetrieveUserAccountRequester(
                RequestSpecs.authUserSpec(createFirstUserRequest.getUsername(), createFirstUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .get(null)
                .extract().as(new TypeRef<>() {
                });

        List<UserAccountsResponse> secondUserAccounts = new RetrieveUserAccountRequester(
                RequestSpecs.authUserSpec(createSecondUserRequest.getUsername(), createSecondUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .get(null)
                .extract().as(new TypeRef<>() {
                });

        UserAccountsResponse retrieveFirstUserAccountsResponse = firstUserAccounts.stream()
                .filter(acc -> acc.getId() == firstUserAccountId)
                .findFirst()
                .orElseThrow();

        UserAccountsResponse retrieveSecondUserAccountsResponse = secondUserAccounts.stream()
                .filter(acc -> acc.getId() == secondUserAccountId)
                .findFirst()
                .orElseThrow();

        int secondUserTransfer = retrieveSecondUserAccountsResponse.getTransactions().size();

        softly.assertThat(retrieveFirstUserAccountsResponse.getBalance()).isEqualTo((deposit * 3));
        softly.assertThat(retrieveSecondUserAccountsResponse.getBalance()).isEqualTo((0.0f));
        softly.assertThat(secondUserTransfer).isEqualTo(0);
    }
}