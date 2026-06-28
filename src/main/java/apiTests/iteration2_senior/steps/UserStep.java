package apiTests.iteration2_senior.steps;

import apiTests.iteration2_senior.models.CreateUserAccountResponse;
import apiTests.iteration2_senior.models.CreateUserRequest;
import apiTests.iteration2_senior.models.LoginUserRequest;
import apiTests.iteration2_senior.models.LoginUserResponse;
import apiTests.iteration2_senior.skelethon.endpoints.Endpoint;
import apiTests.iteration2_senior.skelethon.requests.CrudRequester;
import apiTests.iteration2_senior.specs.Header;
import apiTests.iteration2_senior.specs.RequestSpecs;
import apiTests.iteration2_senior.specs.ResponseSpecs;
import org.hamcrest.Matchers;

public class UserStep {
    private UserStep() {};

    public static LoginUserResponse login(CreateUserRequest createUserRequest) {

        return new CrudRequester(RequestSpecs.unAuthSpec(),
                Endpoint.LOGIN_POST,
                ResponseSpecs.requestReturnsOk())
                .post(LoginUserRequest.builder()
                        .username(createUserRequest.getUsername())
                        .password(createUserRequest.getPassword())
                        .build())
                .header(Header.AUTHORIZATION.getHeader(), Matchers.notNullValue())
                .extract().as(LoginUserResponse.class);
    }

    public static CreateUserAccountResponse createUserAccount(CreateUserRequest createUserRequest) {
        return new CrudRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                Endpoint.ACCOUNTS_POST,
                ResponseSpecs.requestReturnsCreated())
                .post(null)
                .extract()
                .as(CreateUserAccountResponse.class);
    }
}