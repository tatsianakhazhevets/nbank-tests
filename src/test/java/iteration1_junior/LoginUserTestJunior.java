package iteration1_junior;

import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;


import static io.restassured.RestAssured.*;

public class LoginUserTestJunior extends ConfigClassJunior {

    @Test
    public void adminCanGenerateAuthTokenTest() {

        // Жесткая связкаЖ: эндрпоинт , JSON запрос + ответ - жесткая связка всегда проектируется с помощью дата классов или енумов
        given()
                .spec(requestSpecification) // Параметр 1: спецификация запроса (хедеры)
                // Параметер 2: тело запроса
                .body("""
                        {
                          "username": "admin",
                          "password": "admin"
                        }
                        """)
                // Параметр 3: эндпоинт
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=");
        // Параметр 4: спецификация ответа (статус код, проверки)
        //.spec(ResponseSpecification);
    }


    @Test
    public void userCanGenerateAuthTokenTest() {
        //создание пользователя
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "kate2000",
                          "password": "Kate2000#",
                           "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);


        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "username": "kate2000",
                          "password": "Kate2000#"
                        }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .header("Authorization", Matchers.notNullValue());
    }
}