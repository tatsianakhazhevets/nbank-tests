package iteration1;

import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;


import static io.restassured.RestAssured.given;

public class CreateAccountTest extends ConfigClass {

    @Test
    public void userCanCreateAccountTest() {
        //создание пользователя
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "kate200055",
                          "password": "Kate2000#355",
                           "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);


        //получаем токен юзера
        String userAuthHeader = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "username": "kate200055",
                          "password": "Kate2000#355"
                        }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        //создаем аккаунт(счет)
        int accountId = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        //запросить все аккаунты пользователя и проверить, что наш аккаунт там
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", Matchers.hasItem(accountId));
    }
}