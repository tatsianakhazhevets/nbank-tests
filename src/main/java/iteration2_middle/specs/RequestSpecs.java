package iteration2_middle.specs;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import iteration2_middle.models.LoginUserRequest;
import iteration2_middle.requests.LoginUserRequester;

import java.util.List;

public class RequestSpecs {
    private RequestSpecs() {
    }

    private static RequestSpecBuilder defaultRequestSpec() {
        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilters(List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()))
                .setBaseUri("http://localhost:4111");
    }

    public static RequestSpecification unAuthSpec() {
        return defaultRequestSpec()
                .build();
    }

    public static RequestSpecification adminSpec() {
        return defaultRequestSpec()
                .addHeader("Authorization", "Basic YWRtaW46YWRtaW4=")
                .build();
    }

    public static RequestSpecification authUserSpec(String username, String password) {
        String userToken = new LoginUserRequester(RequestSpecs.unAuthSpec(),
                ResponseSpecs.requestReturnsOk())
                .post(LoginUserRequest.builder()
                        .username(username)
                        .password(password)
                        .build())
                .extract()
                .header("Authorization");

        return defaultRequestSpec()
                .addHeader("Authorization", userToken)
                .build();
    }
}