package iteration1_senior.steps;

import iteration1_senior.generators.RandomModelGenerator;
import iteration1_senior.models.CreateAccountResponse;
import iteration1_senior.models.CreateUserRequest;
import iteration1_senior.models.CreateUserResponse;
import iteration1_senior.skelethon.Endpoint;
import iteration1_senior.skelethon.requesters.CrudRequester;
import iteration1_senior.skelethon.requesters.ValidatedCrudRequester;
import iteration1_senior.specs.RequestSpecs;
import iteration1_senior.specs.ResponseSpecs;

public class AdminSteps {
    public static CreateUserRequest createUser() {
        CreateUserRequest userRequest =
                RandomModelGenerator.generate(CreateUserRequest.class);

        new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        return userRequest;
    }
}