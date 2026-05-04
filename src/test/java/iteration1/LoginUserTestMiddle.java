package iteration1;

import iteration1.generators.RandomData;
import iteration1.models.CreateUserRequest;
import iteration1.models.LoginUserRequest;
import iteration1.models.UserRole;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import iteration1.requests.AdminCreateUserRequester;
import iteration1.requests.LoginUserRequester;
import iteration1.specs.RequestSpecs;
import iteration1.specs.ResponseSpecs;

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