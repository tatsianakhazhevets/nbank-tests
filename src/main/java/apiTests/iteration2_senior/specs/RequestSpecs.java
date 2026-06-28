package apiTests.iteration2_senior.specs;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import apiTests.iteration2_senior.configs.Config;
import apiTests.iteration2_senior.models.LoginUserRequest;
import apiTests.iteration2_senior.skelethon.endpoints.Endpoint;
import apiTests.iteration2_senior.skelethon.requests.CrudRequester;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestSpecs {

    private static Map<String, String> authUserTokens = new HashMap<>(Map.of("admin", "Basic YWRtaW46YWRtaW4="));

    private RequestSpecs() {
    }

    private static RequestSpecBuilder defaultRequestSpec() {
        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilters(List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()))
                .setBaseUri(Config.getProperty("apiBaseUrl") + Config.getProperty("apiVersion"));
    }

    public static RequestSpecification unAuthSpec() {
        return defaultRequestSpec()
                .build();
    }

    public static RequestSpecification adminSpec() {
        return defaultRequestSpec()
                .addHeader(Header.AUTHORIZATION.getHeader(), authUserTokens.get("admin"))
                .build();
    }

    public static RequestSpecification authUserSpec(String username, String password) {
        return defaultRequestSpec()
                .addHeader(Header.AUTHORIZATION.getHeader(),
                        getUserAuthHeader(username, password))
                .build();
    }

    public static String getUserAuthHeader(String username, String password) {
        String userAuthHeader;

        if (!authUserTokens.containsKey(username)) {
            userAuthHeader = new CrudRequester(
                    RequestSpecs.unAuthSpec(),
                    Endpoint.LOGIN_POST,
                    ResponseSpecs.requestReturnsOk())
                    .post(LoginUserRequest.builder()
                            .username(username)
                            .password(password)
                            .build())
                    .extract()
                    .header("Authorization");

            authUserTokens.put(username, userAuthHeader);
        } else {
            userAuthHeader = authUserTokens.get(username);
        }

        return userAuthHeader;
    }
}