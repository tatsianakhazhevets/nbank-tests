package iteration2_junior;

import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class ChangeUserNameTest extends ConfigClass {

    private static final String ADMIN_AUTH = "Basic YWRtaW46YWRtaW4=";

    @Test
    public void authorizedUserCanChangeNameSuccessfully() {

        // Admin creates a user
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", ADMIN_AUTH)
                .body("""
                        {
                          "username": "Britta3",
                          "password": "BRIta25!!3",
                           "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        // Get user's token
        String userToken = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "username": "Britta3",
                          "password": "BRIta25!!3"
                        }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // Authorized user can change their name (T29_Positive test)
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userToken)
                .body("""
                        {
                          "name": "Britta Smith"
                        }
                        """)
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("customer.name", Matchers.equalTo("Britta Smith"));

        // Check profile
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userToken)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("name", Matchers.equalTo("Britta Smith"));

        // Authorized user can change their name on the same value (T33_Positive test)
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userToken)
                .body("""
                        {
                          "name": "Britta Smith"
                        }
                        """)
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("customer.name", Matchers.equalTo("Britta Smith"));

        // Check profile
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userToken)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("name", Matchers.equalTo("Britta Smith"));
    }


    public static Stream<Arguments> changeNameInvalidCases() {
        return Stream.of(
                // Authorized user cannot change their name without missed space in one world (T30_Negative test)
                Arguments.of("BrittaPlus11", "HHgg88!11", "USER", "BrittaSmith", "Name must contain two words with letters only"),
                // Authorized user cannot change their name consist of the three worlds (T30_Negative test)
                Arguments.of("BrittaPlus21", "HHgg88!21", "USER", "Britta Smith Jons", "Name must contain two words with letters only"),
                // Authorized user cannot use digits in the name (T31_Negative test)
                Arguments.of("BrittaPlus31", "HHgg88!31", "USER", "Britta1 Smith", "Name must contain two words with letters only"),
                // Authorized user cannot use special signs in the name (T31_Negative test)
                Arguments.of("BrittaPlus41", "HHgg88!41", "USER", "Britta! Smith", "Name must contain two words with letters only"),
                // Authorized user cannot use dash in the name (T32_Negative test)
                Arguments.of("BrittaPlus51", "HHgg88!51", "USER", "Britta-Maria Smith", "Name must contain two words with letters only"),
                // Authorized user cannot use space at the beginning of the name (T34_Negative test)
                Arguments.of("BrittaPlus61", "HHgg88!61", "USER", " Britta Smith", "Name must contain two words with letters only"),
                // Authorized user cannot use space at the end of the name (T35_Negative test)
                Arguments.of("BrittaPlus71", "HHgg88!71", "USER", "Britta Smith ", "Name must contain two words with letters only")
        );
    }

    @MethodSource("changeNameInvalidCases")
    @ParameterizedTest
    public void authorizedUserCannotChangeNameWithInvalidData(String username, String password, String role,
                                                              String name, String errorMessage) {

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

        String changeNameRequestBody = String.format(
                """
                        {
                          "name": "%s"
                        }
                        """, name);

        // Check name change
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userToken)
                .body(changeNameRequestBody)
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.equalTo(errorMessage));

        // Check profile
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userToken)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("name", Matchers.not(Matchers.equalTo(name)))
                .body("name", Matchers.nullValue());
    }
}