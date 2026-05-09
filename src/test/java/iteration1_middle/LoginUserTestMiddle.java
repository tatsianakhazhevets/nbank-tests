package iteration1_middle;

import iteration1_middle.generators.RandomData;
import iteration1_middle.models.CreateUserRequest;
import iteration1_middle.models.LoginUserRequest;
import iteration1_middle.models.UserRole;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import iteration1_middle.requests.AdminCreateUserRequester;
import iteration1_middle.requests.LoginUserRequester;
import iteration1_middle.specs.RequestSpecs;
import iteration1_middle.specs.ResponseSpecs;

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