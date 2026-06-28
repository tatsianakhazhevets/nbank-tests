package apiTests.iteration2_senior.steps;

import io.restassured.common.mapper.TypeRef;
import apiTests.iteration2_senior.generators.RandomModelGenerator;
import apiTests.iteration2_senior.models.AdminUsersResponse;
import apiTests.iteration2_senior.models.CreateUserRequest;
import apiTests.iteration2_senior.models.CreateUserResponse;
import apiTests.iteration2_senior.skelethon.endpoints.Endpoint;
import apiTests.iteration2_senior.skelethon.requests.CrudRequester;
import apiTests.iteration2_senior.skelethon.requests.ValidatedCrudRequester;
import apiTests.iteration2_senior.specs.RequestSpecs;
import apiTests.iteration2_senior.specs.ResponseSpecs;

import java.util.List;

public class AdminStep {

    public static CreateUserRequest createUser() {
        CreateUserRequest createUserRequest = RandomModelGenerator.generate(CreateUserRequest.class);

        new ValidatedCrudRequester<CreateUserResponse>(RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USERS_POST,
                ResponseSpecs.requestReturnsCreated())
                .post(createUserRequest);

        return createUserRequest;
    }

    public static void deleteUsers() {
        List<AdminUsersResponse> users = new ValidatedCrudRequester<AdminUsersResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USERS_GET,
                ResponseSpecs.requestReturnsOk())
                .getAll(AdminUsersResponse[].class);

        for (AdminUsersResponse user : users) {
            try {
                new CrudRequester(
                        RequestSpecs.adminSpec(),
                        Endpoint.ADMIN_USERS_DELETE,
                        ResponseSpecs.requestReturnsOk())
                        .delete(user.getId());
            } catch (Exception e) {
                System.out.println("Failed to delete user.getId()=" + user.getId());
            }
        }
    }


    public static List<CreateUserResponse> getAllUsers() {
        return new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USERS_GET,
                ResponseSpecs.requestReturnsOk())
                .getAll(CreateUserResponse[].class);
    }
}