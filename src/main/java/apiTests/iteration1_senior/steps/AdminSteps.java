package apiTests.iteration1_senior.steps;

import apiTests.iteration1_senior.generators.RandomModelGenerator;
import apiTests.iteration1_senior.models.CreateUserRequest;
import apiTests.iteration1_senior.models.CreateUserResponse;
import apiTests.iteration1_senior.skelethon.Endpoint;
import apiTests.iteration1_senior.skelethon.requesters.ValidatedCrudRequester;
import apiTests.iteration1_senior.specs.RequestSpecs;
import apiTests.iteration1_senior.specs.ResponseSpecs;

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