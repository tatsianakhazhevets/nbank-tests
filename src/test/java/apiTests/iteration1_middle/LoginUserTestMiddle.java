package apiTests.iteration1_middle;

import apiTests.iteration1_middle.generators.RandomData;
import apiTests.iteration1_middle.models.CreateUserRequest;
import apiTests.iteration1_middle.models.LoginUserRequest;
import apiTests.iteration1_middle.models.UserRole;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import apiTests.iteration1_middle.requests.AdminCreateUserRequester;
import apiTests.iteration1_middle.requests.LoginUserRequester;
import apiTests.iteration1_middle.specs.RequestSpecs;
import apiTests.iteration1_middle.specs.ResponseSpecs;

import static io.restassured.RestAssured.given;

public class LoginUserTestMiddle extends BaseTestMiddle {

    @Test
    public void adminCanGenerateAuthTokenTest() {
        LoginUserRequest userRequest = LoginUserRequest.builder()
                .username("admin")
                .password("admin")
                .build();

        new LoginUserRequester(RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnOK())
                .post(userRequest);
    }

    @Test
    public void userCanGenerateAuthTokenTest() {
        //создание пользователя
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        //создание пользователя
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        new LoginUserRequester(RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnOK())
                .post(LoginUserRequest.builder().username(userRequest.getUsername()).password(userRequest.getPassword()).build())
                .header("Authorization", Matchers.notNullValue());
    }
}