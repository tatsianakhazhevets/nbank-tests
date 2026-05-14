package iteration1_senior;

import iteration1_senior.models.CreateUserRequest;
import iteration1_senior.models.CreateUserResponse;
import iteration1_senior.models.LoginUserRequest;
import iteration1_senior.skelethon.Endpoint;
import iteration1_senior.skelethon.requesters.CrudRequester;
import iteration1_senior.skelethon.requesters.ValidatedCrudRequester;
import iteration1_senior.specs.RequestSpecs;
import iteration1_senior.specs.ResponseSpecs;
import iteration1_senior.steps.AdminSteps;
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