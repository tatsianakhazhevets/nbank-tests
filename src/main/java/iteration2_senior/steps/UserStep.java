package iteration2_senior.steps;

import iteration2_senior.models.CreateUserAccountResponse;
import iteration2_senior.models.CreateUserRequest;
import iteration2_senior.models.LoginUserRequest;
import iteration2_senior.models.LoginUserResponse;
import iteration2_senior.skelethon.endpoints.Endpoint;
import iteration2_senior.skelethon.requests.CrudRequester;
import iteration2_senior.specs.Header;
import iteration2_senior.specs.RequestSpecs;
import iteration2_senior.specs.ResponseSpecs;
import org.hamcrest.Matchers;

public class UserStep {

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