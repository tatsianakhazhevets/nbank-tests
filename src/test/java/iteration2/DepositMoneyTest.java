package iteration2;

import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Locale;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class DepositMoneyTest extends ConfigClass {

    private static final String ADMIN_AUTH = "Basic YWRtaW46YWRtaW4=";

    public static Stream<Arguments> depositPositiveCases() {
        return Stream.of(
                // Authorized user can deposit money (T1_Positive test)
                Arguments.of("Inga4", "AAbb11!!4", "USER", 2500),
                // Authorized user can deposit 5000.00 (T2_Positive test)
                Arguments.of("Kate4", "BBaa11!!4", "USER", 5000),
                // Authorized user can deposit 4999.99 (T3_Positive test)
                Arguments.of("Boris4", "GGbb11!!4", "USER", 4999.99),
                // Deposit amount must be positive – test with 0,01 (T5_Positive test)
                Arguments.of("Anna4", "GGyy11!!4", "USER", 0.01),
                // Deposit amount must be positive – test with 0,02 (T6_Positive test)
                Arguments.of("Olga4", "GGyy11!!4", "USER", 0.02)
        );
    }

    @MethodSource("depositPositiveCases")
    @ParameterizedTest
    public void authorizedUserDepositsMoneySuccessfully(String username, String password, String role, double deposit) {

        String createUserRequestBody = String.format(
                """
                        {
                          "username": "%s",
                          "password": "%s",
                           "role": "%s"
                        }
                        """, username, password, role);

        // Admin creates a user
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

        // Get user's token
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

        // Authorized user creates the account
        int accountId = given()
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
                        """, accountId, deposit);

        // Authorized user deposit money
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
                .body("[0].transactions[0].amount", Matchers.equalTo((float) deposit));
    }


    public static Stream<Arguments> depositInvalidCases() {
        return Stream.of(
                // Authorized user cannot deposit 5000.01 (T4_Negative)
                Arguments.of("Inga15", "AAbb11!!15", "USER", 5000.01, "Deposit amount cannot exceed 5000"),
                // Deposit amount must be positive – test with 0,00 (T7_Negative)
                Arguments.of("Anna15", "GGyy11!!15", "USER", 0.00, "Deposit amount must be at least 0.01")
        );
    }

    @MethodSource("depositInvalidCases")
    @ParameterizedTest
    public void authorizedUserCannotDepositInvalidAmount(String username, String password, String role, double deposit,
                                                         String errorMessage) {

        String createUserRequestBody = String.format(
                """
                        {
                          "username": "%s",
                          "password": "%s",
                           "role": "%s"
                        }
                        """, username, password, role);

        // Admin creates a user
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

        // Get user's token
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

        // Authorized user creates the account
        int accountId = given()
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
                        """, accountId, deposit);

        // Authorized user cannot deposit money
        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(depositMoneyRequestBody)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.equalTo(errorMessage));
    }


    @Test
    public void authorizedUserCannotDepositToNonExistingAccount() {

        // Admin creates a user
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", ADMIN_AUTH)
                .body("""
                        {
                          "username": "Maria5",
                          "password": "KKdd11!!5",
                           "role": "USER"
                        }
                        """
                )
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        // Get user's token
        String userToken = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(
                        """
                                {
                                  "username": "Maria5",
                                  "password": "KKdd11!!5"
                                }
                                """
                )
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // Authorized user cannot deposit money without account (T8_Negative test)
        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                              {
                                "id": 1,
                                "balance": 1000.50
                              }
                        """
                )
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .body(Matchers.equalTo("Unauthorized access to account"));
    }


    @Test
    public void authorizedUserCannotDepositToAnotherUsersAccount() {

        // Admin creates the first user
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", ADMIN_AUTH)
                .body("""
                        {
                          "username": "Anastasia4",
                          "password": "LLll55!!4",
                           "role": "USER"
                        }
                        """
                )
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        // Get the first user's token
        String firstUserToken = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(
                        """
                                      {
                                         "username": "Anastasia4",
                                         "password": "LLll55!!4"
                                      }
                                """
                )
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // Admin creates the second user
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", ADMIN_AUTH)
                .body("""
                        {
                          "username": "Alex4",
                          "password": "LLxx55!!4",
                           "role": "USER"
                        }
                        """
                )
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        // Get the second user's token
        String secondUserToken = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(
                        """
                                      {
                                         "username": "Alex4",
                                         "password": "LLxx55!!4"
                                      }
                                """
                )
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

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

        // Authorized first user cannot deposit money to another authorized user (T9_Negative test)
        given()
                .header("Authorization", firstUserToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format("""
                              {
                                "id": %d,
                                "balance": 1000.50
                              }
                        """, secondUserAccountId
                ))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .body(Matchers.equalTo("Unauthorized access to account"));
    }
}