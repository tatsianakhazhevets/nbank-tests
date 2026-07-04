package apiTests.iteration2_senior.steps;

import apiTests.iteration2_senior.models.*;
import apiTests.iteration2_senior.skelethon.endpoints.Endpoint;
import apiTests.iteration2_senior.skelethon.requests.CrudRequester;
import apiTests.iteration2_senior.skelethon.requests.ValidatedCrudRequester;
import apiTests.iteration2_senior.specs.Header;
import apiTests.iteration2_senior.specs.RequestSpecs;
import apiTests.iteration2_senior.specs.ResponseSpecs;
import org.hamcrest.Matchers;

import java.util.List;

public class UserStep {
    private String username;
    private String password;

    public UserStep(String username, String password) {
        this.username = username;
        this.password = password;
    }

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

    public List<AccountsNestedResponse> getAllAccounts() {
        return new ValidatedCrudRequester<AccountsNestedResponse>(
                RequestSpecs.authUserSpec(username, password),
                Endpoint.CUSTOMER_ACCOUNTS_GET,
                ResponseSpecs.requestReturnsOk())
                .getAll(AccountsNestedResponse[].class);
    }
}