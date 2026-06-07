package iteration1;

import iteration1.generators.RandomData;
import iteration1.models.CreateUserRequest;
import iteration1.models.UserRole;
import org.junit.jupiter.api.Test;
import iteration1.requests.AdminCreateUserRequester;
import iteration1.requests.CreateAccountRequester;
import iteration1.specs.RequestSpecs;
import iteration1.specs.ResponseSpecs;

public class CreateAccountTestMiddle {

    @Test
    public void userCanCreateAccountTest() {

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

        new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null);
    }
}
