package apiTests.iteration2_junior;

import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Locale;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class TransferMoneyTest extends ConfigClass {

    private static final String ADMIN_AUTH = "Basic YWRtaW46YWRtaW4=";

    public static Stream<Arguments> validTransfersBetweenOwnAccounts() {
        return Stream.of(
                // Authorized user can transfer money between their accounts (T11_Positive test)
                Arguments.of("Alex1", "SSqq11!!1", "USER", 2500, 100),
                // Authorized user can transfer 10000.00 between their accounts (T12_Positive test)
                Arguments.of("Sasha1", "SSss11!!1", "USER", 5000, 10000.00),
                // Authorized user can transfer 9999.99 between their accounts (T13_Positive test)
                Arguments.of("Pavel1", "SSpp11!!1", "USER", 5000, 9999.99),
                // Transfer amount must be positive – test with 0,01 (T19_Positive test)
                Arguments.of("Nina1", "SSnn11!!1", "USER", 5000, 0.01),
                // Transfer amount must be positive – test with 0,02 (T20_Positive test)
                Arguments.of("Misha1", "SSmm11!!1", "USER", 5000, 0.02),
                // Authorized user can transfer less than 10000.00 and equals to their balance (T23_Positive test)
                Arguments.of("Denis1", "DDmm11!!1", "USER", 2000, 4000),
                // Authorized first user can transfer less than 10000 and equals to their balance minus 0.01 (T26_Positive test)
                Arguments.of("Alena1", "SSll11!!1", "USER", 2000, 3999.99)
        );
    }

    @MethodSource("validTransfersBetweenOwnAccounts")
    @ParameterizedTest
    public void userTransfersMoneyBetweenOwnAccountsSuccessfully(String username, String password, String role,
                                                                 double deposit, double transferAmount) {

        String createUserRequestBody = String.format(
                """
                        {
                          "username": "%s",
                          "password": "%s",
                           "role": "%s"
                        }
                        """, username, password, role);

        // Admin creates user
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", ADMIN_AUTH)
                .body(createUserRequestBody)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        String getTokenRequestBody = String.format(
                """
                        {
                          "username": "%s",
                          "password": "%s"
                        }
                        """, username, password);

        // Get the user's token
        String userToken = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(getTokenRequestBody)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // Authorized user creates the first account
        int firstAccountId = given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        String depositMoneyRequestBody = String.format(
                Locale.US,
                """
                        {
                          "id": %d,
                          "balance": %.2f
                        }
                        """, firstAccountId, deposit);

        // Authorized user deposit money (the first deposit)
        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(depositMoneyRequestBody)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // Authorized user deposit money (the second deposit)
        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(depositMoneyRequestBody)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // Check deposit
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == " + firstAccountId + " }.balance", Matchers.equalTo((float) (deposit + deposit)));

        // Authorized user creates the second account
        int secondAccountId = given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        String transferMoneyRequestBody = String.format(
                Locale.US,
                """
                        {
                          "senderAccountId": %d,
                          "receiverAccountId": %d,
                          "amount": %.2f
                        }
                        """, firstAccountId, secondAccountId, transferAmount);

        // Authorized user transfer money between their accounts
        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(transferMoneyRequestBody)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // Check transfer
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == " + secondAccountId + " }.transactions.find { it.type == 'TRANSFER_IN' }.amount",
                        Matchers.equalTo((float) transferAmount))
                .body("find { it.id == " + firstAccountId + " }.transactions.find { it.type == 'TRANSFER_OUT' }.amount",
                        Matchers.equalTo((float) transferAmount))
                .body("find { it.id == " + firstAccountId + " }.balance",
                        Matchers.equalTo((float) ((deposit + deposit) - transferAmount)))
                .body("find { it.id == " + secondAccountId + " }.balance",
                        Matchers.equalTo((float) (transferAmount)));
    }


    public static Stream<Arguments> invalidTransfersBetweenOwnAccounts() {
        return Stream.of(
                // Authorized first user can not transfer 10000.01 between their accounts (T15_Negative test)
                Arguments.of("Ilia4", "LLll11!!4", "USER", 5000, 10000.01, "Transfer amount cannot exceed 10000"),
                // Transfer amount must be positive – test with 0.00 within first user accounts (T21_Negative test)
                Arguments.of("Geret4", "GGgg11!!4", "USER", 50, 0.00, "Transfer amount must be at least 0.01"),
                // Authorized first user cannot transfer less than their balance (balance plus 0.01) between their accounts (T27_Negative test)
                Arguments.of("Luice4", "IIii11!!4", "USER", 300, 901, "Invalid transfer: insufficient funds or invalid accounts")
        );
    }

    @MethodSource("invalidTransfersBetweenOwnAccounts")
    @ParameterizedTest
    public void userCannotTransferInvalidAmountsBetweenAccounts(String username, String password, String role,
                                                                double deposit, double transferAmount, String errorMessage) {

        String createUserRequestBody = String.format(
                """
                        {
                          "username": "%s",
                          "password": "%s",
                           "role": "%s"
                        }
                        """, username, password, role);

        // Admin creates user
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", ADMIN_AUTH)
                .body(createUserRequestBody)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        String getTokenRequestBody = String.format(
                """
                        {
                          "username": "%s",
                          "password": "%s"
                        }
                        """, username, password);

        // Get the user's token
        String userToken = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(getTokenRequestBody)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // Authorized user creates the first account
        int firstAccountId = given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        String depositMoneyRequestBody = String.format(
                Locale.US,
                """
                        {
                          "id": %d,
                          "balance": %.2f
                        }
                        """, firstAccountId, deposit);

        // Authorized user deposit money (the first deposit)
        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(depositMoneyRequestBody)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // Authorized user deposit money (the second deposit)
        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(depositMoneyRequestBody)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // Authorized user deposit money (the third deposit)
        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(depositMoneyRequestBody)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // Check deposit
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == " + firstAccountId + " }.balance", Matchers.equalTo((float) (deposit * 3)));

        // Authorized user creates the second account
        int secondAccountId = given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        String transferMoneyRequestBody = String.format(
                Locale.US,
                """
                        {
                          "senderAccountId": %d,
                          "receiverAccountId": %d,
                          "amount": %.2f
                        }
                        """, firstAccountId, secondAccountId, transferAmount);

        // Authorized user cannot transfer money between their accounts
        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(transferMoneyRequestBody)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.equalTo(errorMessage));

        // Check transfer
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == " + firstAccountId + " }.balance", Matchers.equalTo((float) (deposit * 3)))
                .body("find { it.id == " + secondAccountId + " }.transactions.size()", Matchers.equalTo(0))
                .body("find { it.id == " + secondAccountId + " }.balance", Matchers.equalTo(0.0f));
    }


    public static Stream<Arguments> validTransferToAnotherUser() {
        return Stream.of(
                // Authorized first user can transfer money to the authorized second user (T10_Positive test)
                Arguments.of("IrinaFirst13", "QQqq11!!13", "IrinaSecond13", "qqQQ11!!13", "USER", 2500, 100),
                // Authorized first user can transfer 10000.00 to authorized second user (T16_Positive test)
                Arguments.of("OlegFirst3", "OOqq11!!3", "OlegSecond3", "qqOO11!!3", "USER", 5000, 10000),
                // Authorized first user can transfer 9999.99 to authorized second user (T17_Positive test)
                Arguments.of("KirillFirst3", "KKqq11!!3", "KirillSecond3", "qqKK11!!3", "USER", 5000, 9999.99)
        );
    }

    @MethodSource("validTransferToAnotherUser")
    @ParameterizedTest
    public void userCanTransferMoneyToAnotherUserSuccessfully(String username1, String password1,
                                                              String username2, String password2,
                                                              String role, double deposit, double transferAmount) {

        String createFirstUserRequestBody = String.format(
                """
                        {
                          "username": "%s",
                          "password": "%s",
                           "role": "%s"
                        }
                        """, username1, password1, role);

        // Admin creates the first user
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", ADMIN_AUTH)
                .body(createFirstUserRequestBody)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        String getTokenFirstUserRequestBody = String.format(
                """
                        {
                          "username": "%s",
                          "password": "%s"
                        }
                        """, username1, password1);

        // Get the first user's token
        String firstUserToken = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(getTokenFirstUserRequestBody)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // Authorized first user creates the account
        int firstUserAccountId = given()
                .header("Authorization", firstUserToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        String depositMoneyRequestBody = String.format(
                Locale.US,
                """
                        {
                          "id": %d,
                          "balance": %.2f
                        }
                        """, firstUserAccountId, deposit);

        // Authorized first user deposit money (the first deposit)
        given()
                .header("Authorization", firstUserToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(depositMoneyRequestBody)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // Authorized first user deposit money (the second deposit)
        given()
                .header("Authorization", firstUserToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(depositMoneyRequestBody)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // Check deposit
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", firstUserToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == " + firstUserAccountId + " }.balance", Matchers.equalTo((float) (deposit + deposit)));

        String createSecondUserRequestBody = String.format(
                """
                        {
                          "username": "%s",
                          "password": "%s",
                          "role": "%s"
                        }
                        """, username2, password2, role);

        // Admin creates the second user
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", ADMIN_AUTH)
                .body(createSecondUserRequestBody)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        String getTokenSecondUserRequestBody = String.format(
                """
                        {
                          "username": "%s",
                          "password": "%s"
                        }
                        """, username2, password2);

        // Get the second user's token
        String secondUserToken = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(getTokenSecondUserRequestBody)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // Authorized second user creates the account
        int secondUserAccountId = given()
                .header("Authorization", secondUserToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        String transferRequestBody = String.format(
                Locale.US,
                """
                        {
                          "senderAccountId": %d,
                          "receiverAccountId": %d,
                          "amount": %.2f
                        }
                        """, firstUserAccountId, secondUserAccountId, transferAmount);

        // Authorized first user transfer money to authorized second user
        given()
                .header("Authorization", firstUserToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(transferRequestBody)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // Check transfer on second user account
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", secondUserToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == " + secondUserAccountId + " }.transactions.find { it.type == 'TRANSFER_IN' }.amount",
                        Matchers.equalTo((float) transferAmount))
                .body("find { it.id == " + secondUserAccountId + " }.balance", Matchers.equalTo((float) transferAmount));

        // Check transfer on first user account
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", firstUserToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == " + firstUserAccountId + " }.balance",
                        Matchers.equalTo((float) ((deposit + deposit) - transferAmount)))
                .body(
                        "find { it.id == " + firstUserAccountId + " }.transactions.find { it.type == 'TRANSFER_OUT' }.amount",
                        Matchers.equalTo((float) transferAmount));
    }


    public static Stream<Arguments> invalidTransferToAnotherUser() {
        return Stream.of(
                // Authorized first user cannot transfer 10000.01 to authorized second user (T18_Negative test)
                Arguments.of("DenisFirst23", "DDdd22!!23", "DenisSecond23", "INin22!!23", "USER", 5000, 10000.01, "Transfer amount cannot exceed 10000"),
                // Transfer amount must be positive – test with 0.00 among accounts of the different users (T22_Negative test)
                Arguments.of("FedyaFirst23", "FEfe11!!23", "FedyaSecond23", "DYdy11!!23", "USER", 200, 0.00, "Transfer amount must be at least 0.01"),
                // Authorized first user cannot transfer less than their balance (balance plus 0.1) to account of authorized second user (T28_Negative test)
                Arguments.of("MaxFirst13", "MFmf11!!13", "MaxSecond13", "mfMF11!!13", "USER", 300, 900.01, "Invalid transfer: insufficient funds or invalid accounts")
        );
    }

    @MethodSource("invalidTransferToAnotherUser")
    @ParameterizedTest
    public void userCannotTransferMoneyToAnotherUser(String username1, String password1, String username2, String password2,
                                                     String role, double deposit, double transferAmount, String errorMessage) {

        String createFirstUserRequestBody = String.format(
                """
                        {
                          "username": "%s",
                          "password": "%s",
                           "role": "%s"
                        }
                        """, username1, password1, role);

        // Admin creates the first user
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", ADMIN_AUTH)
                .body(createFirstUserRequestBody)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        String getTokenFirstUserRequestBody = String.format(
                """
                        {
                          "username": "%s",
                          "password": "%s"
                        }
                        """, username1, password1);

        // Get the first user's token
        String firstUserToken = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(getTokenFirstUserRequestBody)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // Authorized first user creates the account
        int firstUserAccountId = given()
                .header("Authorization", firstUserToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        String depositMoneyRequestBody = String.format(
                Locale.US,
                """
                        {
                          "id": %d,
                          "balance": %.2f
                        }
                        """, firstUserAccountId, deposit);

        // Authorized first user deposit money (the first deposit)
        given()
                .header("Authorization", firstUserToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(depositMoneyRequestBody)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // Authorized first user deposit money (the second deposit)
        given()
                .header("Authorization", firstUserToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(depositMoneyRequestBody)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // Authorized first user deposit money (the third deposit)
        given()
                .header("Authorization", firstUserToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(depositMoneyRequestBody)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // Check deposit
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", firstUserToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == " + firstUserAccountId + " }.balance", Matchers.equalTo((float) (deposit * 3)));

        String createSecondUserRequestBody = String.format(
                """
                        {
                          "username": "%s",
                          "password": "%s",
                          "role": "%s"
                        }
                        """, username2, password2, role);

        // Admin creates the second user
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", ADMIN_AUTH)
                .body(createSecondUserRequestBody)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        String getTokenSecondUserRequestBody = String.format(
                """
                        {
                          "username": "%s",
                          "password": "%s"
                        }
                        """, username2, password2);

        // Get the second user's token
        String secondUserToken = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(getTokenSecondUserRequestBody)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // Authorized second user creates the account
        int secondUserAccountId = given()
                .header("Authorization", secondUserToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        String transferRequestBody = String.format(
                Locale.US,
                """
                        {
                          "senderAccountId": %d,
                          "receiverAccountId": %d,
                          "amount": %.2f
                        }
                        """, firstUserAccountId, secondUserAccountId, transferAmount);

        // Check transfer
        given()
                .header("Authorization", firstUserToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(transferRequestBody)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.equalTo(errorMessage));

        // Check transfer on second user account
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", secondUserToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == " + secondUserAccountId + " }.transactions.size()", Matchers.equalTo(0))
                .body("find { it.id == " + secondUserAccountId + " }.balance", Matchers.equalTo(0.0f));

        // Check transfer on first user account
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", firstUserToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == " + firstUserAccountId + " }.balance", Matchers.equalTo((float) (deposit * 3)));
    }
}