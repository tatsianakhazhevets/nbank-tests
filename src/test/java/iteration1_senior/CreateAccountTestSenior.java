package iteration1_senior;

import iteration1_senior.models.CreateUserRequest;
import iteration1_senior.skelethon.Endpoint;
import iteration1_senior.skelethon.requesters.CrudRequester;
import iteration1_senior.specs.RequestSpecs;
import iteration1_senior.specs.ResponseSpecs;
import iteration1_senior.steps.AdminSteps;
import org.junit.jupiter.api.Test;

public class CreateAccountTestSenior {
    @Test
    public void userCanCreateAccountTest() {

        CreateUserRequest userRequest = AdminSteps.createUser();

        new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);
    }
    // запросить все аккаунты пользователя и проверить, что наш аккаунт там
}