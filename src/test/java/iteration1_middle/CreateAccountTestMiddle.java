package iteration1_middle;

import iteration1_middle.generators.RandomData;
import iteration1_middle.models.CreateUserRequest;
import iteration1_middle.models.UserRole;
import org.junit.jupiter.api.Test;
import iteration1_middle.requests.AdminCreateUserRequester;
import iteration1_middle.requests.CreateAccountRequester;
import iteration1_middle.specs.RequestSpecs;
import iteration1_middle.specs.ResponseSpecs;

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

    // запросить все аккаунты пользователя и проверить, что наш аккаунт там
}