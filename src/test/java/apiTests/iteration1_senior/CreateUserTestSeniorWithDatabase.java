package apiTests.iteration1_senior;

import apiTests.iteration1_senior.dao.UserDao;
import apiTests.iteration1_senior.dao.comparison.DaoAndModelAssertions;
import apiTests.iteration1_senior.generators.RandomModelGenerator;
import apiTests.iteration1_senior.models.CreateUserRequest;
import apiTests.iteration1_senior.models.CreateUserResponse;
import apiTests.iteration1_senior.models.comparison.ModelAssertions;
import apiTests.iteration1_senior.skelethon.Endpoint;
import apiTests.iteration1_senior.skelethon.requesters.CrudRequester;
import apiTests.iteration1_senior.skelethon.requesters.ValidatedCrudRequester;
import apiTests.iteration1_senior.specs.RequestSpecs;
import apiTests.iteration1_senior.specs.ResponseSpecs;
import apiTests.iteration1_senior.steps.DataBaseSteps;
import common.annotations.APIVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.assertNull;

@APIVersion("with_database_with_fix")
public class CreateUserTestSeniorWithDatabase extends BaseTestSenior {
    @Test
    public void adminCanCreateUserWithCorrectData() {
        //Подготовка данных
        CreateUserRequest createUserRequest =
                RandomModelGenerator.generate(CreateUserRequest.class);

        //POST запрос
        CreateUserResponse createUserResponse = new ValidatedCrudRequester<CreateUserResponse>
                (RequestSpecs.adminSpec(),
                        Endpoint.ADMIN_USER,
                        ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        //GET запрос для проверки созданного юзера

        ModelAssertions.assertThatModels(createUserRequest, createUserResponse).match();

        //Проверка через базу данных
        UserDao userDao = DataBaseSteps.getUserByUsername(createUserRequest.getUsername());
        DaoAndModelAssertions.assertThat(createUserResponse, userDao).match();
    }

    public static Stream<Arguments> userInvalidData() {
        return Stream.of(
                //username field validation
                Arguments.of(" ", "Password33$", "USER", "username", List.of("Username cannot be blank",
                        "Username must contain only letters, digits, dashes, underscores, and dots", "Username must be between 3 and 15 characters")),
                Arguments.of("ab", "Password33$", "USER", "username", List.of("Username must be between 3 and 15 characters")),
                Arguments.of("abd%", "Password33$", "USER", "username", List.of("Username must contain only letters, digits, dashes, underscores, and dots")),
                Arguments.of("abd$", "Password33$", "USER", "username", List.of("Username must contain only letters, digits, dashes, underscores, and dots"))
        );
    }

    @MethodSource("userInvalidData")
    @ParameterizedTest
    public void adminCanNotCreateUserWithInvalidData(String username, String password, String role, String errorKey, List<String> errorValue) {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(role)
                .build();

        new CrudRequester(RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.requestReturnsBadRequest(errorKey, errorValue))
                .post(createUserRequest);

        assertNull(DataBaseSteps.getUserByUsername(createUserRequest.getUsername()));
    }
}