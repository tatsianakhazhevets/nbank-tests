package uiTests.iteration1_senior;

import apiTests.iteration1_senior.models.comparison.ModelAssertions;
import apiTests.iteration2_senior.generators.RandomModelGenerator;
import apiTests.iteration2_senior.models.CreateUserRequest;
import apiTests.iteration2_senior.models.CreateUserResponse;
import apiTests.iteration2_senior.steps.AdminStep;
import common.annotations.AdminSession;
import org.junit.jupiter.api.Test;
import uiTests.iteration1_middle.pages.AdminPanelSenior;
import uiTests.iteration1_middle.pages.BankAlert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class CreateUserSeniorTest extends BaseUiTest {

    @Test
    @AdminSession
    public void adminCanCreateUserTest() {
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);

        assertTrue(new AdminPanelSenior()
                .open()
                .createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertMessageAndAccept(BankAlert.USER_CREATED_SUCCESSFULLY.getMessage())
                .getAllUsers()
                .stream()
                .anyMatch(userBage -> userBage.getUsername().equals(newUser.getUsername())));

        CreateUserResponse createUser = AdminStep.getAllUsers().stream()
                .filter(user -> user.getUsername().equals(newUser.getUsername()))
                .findFirst().get();

        ModelAssertions.assertThatModels(newUser, createUser).match();
    }

    @Test
    @AdminSession //JUnit Extension
    public void adminCannotCreateUserWithInvalidDataTest() {
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        newUser.setUsername("a");

        assertTrue(new AdminPanelSenior()
                .open()
                .createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertMessageAndAccept(BankAlert.USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS.getMessage())
                .getAllUsers().stream().noneMatch(userBage -> userBage.getUsername().equals(newUser.getUsername())));

        long usersWithSameUsernameAsNewUser = AdminStep.getAllUsers().stream()
                .filter(user -> user.getUsername().equals(newUser.getUsername()))
                .count();

        assertThat(usersWithSameUsernameAsNewUser).isZero();
    }
}