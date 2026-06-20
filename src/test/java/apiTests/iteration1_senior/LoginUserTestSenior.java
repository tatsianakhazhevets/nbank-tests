package apiTests.iteration1_senior;

import apiTests.iteration1_senior.models.CreateUserRequest;
import apiTests.iteration1_senior.models.CreateUserResponse;
import apiTests.iteration1_senior.models.LoginUserRequest;
import apiTests.iteration1_senior.skelethon.Endpoint;
import apiTests.iteration1_senior.skelethon.requesters.CrudRequester;
import apiTests.iteration1_senior.skelethon.requesters.ValidatedCrudRequester;
import apiTests.iteration1_senior.specs.RequestSpecs;
import apiTests.iteration1_senior.specs.ResponseSpecs;
import apiTests.iteration1_senior.steps.AdminSteps;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

public class LoginUserTestSenior {
    @Test
    public void adminCanGenerateAuthTokenTest() {
        LoginUserRequest userRequest = LoginUserRequest.builder()
                .username("admin")
                .password("admin")
                .build();

        new ValidatedCrudRequester<CreateUserResponse>(RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnOK())
                .post(userRequest);
    }

    @Test
    public void userCanGenerateAuthTokenTest() {
        CreateUserRequest userRequest = AdminSteps.createUser();

        new CrudRequester(RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnOK())
                .post(LoginUserRequest.builder().username(userRequest.getUsername()).password(userRequest.getPassword()).build())
                .header("Authorization", Matchers.notNullValue());
    }
}