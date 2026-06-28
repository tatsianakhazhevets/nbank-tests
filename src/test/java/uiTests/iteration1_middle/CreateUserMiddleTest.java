package uiTests.iteration1_middle;

import apiTests.iteration1_senior.models.comparison.ModelAssertions;
import apiTests.iteration2_senior.generators.RandomModelGenerator;
import apiTests.iteration2_senior.models.CreateUserRequest;
import apiTests.iteration2_senior.models.CreateUserResponse;
import apiTests.iteration2_senior.steps.AdminStep;
import com.codeborne.selenide.Condition;
import org.junit.jupiter.api.Test;
import uiTests.iteration1_middle.pages.AdminPanel;
import uiTests.iteration1_middle.pages.BankAlert;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateUserMiddleTest extends BaseUiTest {
    @Test
    public void adminCanCreateUserTest() {
        //ШАГ 1: Админ залогинился в банке
        CreateUserRequest admin = CreateUserRequest.getAdmin();
        authAsUser(admin);

        //ШАГ 2: Админ создает юзера в банке
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);

        new AdminPanel()
                .open()
                .createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertMessageAndAccept(BankAlert.USER_CREATED_SUCCESSFULLY.getMessage())
                .getAllUsers()
                .findBy(Condition.exactText(newUser.getUsername() + "\nUSER"))
                .shouldBe(Condition.visible);

        //Шаг 5: Проверка, что юзер создан на API
        CreateUserResponse createUser = AdminStep.getAllUsers().stream()
                .filter(user -> user.getUsername().equals(newUser.getUsername()))
                .findFirst().get();

        ModelAssertions.assertThatModels(newUser, createUser).match();
    }

    @Test
    public void adminCannotCreateUserWithInvalidDataTest() {
        //ШАГ 1: Админ залогинился в банке
        CreateUserRequest admin = CreateUserRequest.getAdmin();
        authAsUser(admin);

        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        newUser.setUsername("a");

        new AdminPanel()
                .open()
                .createUser(newUser.getUsername(), newUser.getPassword())
                        .checkAlertMessageAndAccept(BankAlert.USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS.getMessage())
                .getAllUsers().findBy(Condition.exactText(newUser.getUsername() + "\nUSER"))
                .shouldNotBe(Condition.exist);

        //ШАГ 5: Проверка, что юзер НЕ создан на API
        long usersWithSameUsernameAsNewUser = AdminStep.getAllUsers().stream()
                .filter(user -> user.getUsername().equals(newUser.getUsername()))
                .count();

        assertThat(usersWithSameUsernameAsNewUser).isZero();
    }
}