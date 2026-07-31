package uiTests.iteration1_middle;

import apiTests.iteration2_senior.models.CreateUserRequest;
import apiTests.iteration2_senior.steps.AdminStep;
import com.codeborne.selenide.Condition;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import uiTests.iteration1_middle.pages.AdminPanel;
import uiTests.iteration1_middle.pages.LoginPage;
import uiTests.iteration1_middle.pages.UserDashboard;

public class LoginUserMiddleTest extends BaseUiTest {
    @Disabled("learning purpose")
    public void adminCanLoginWithCorrectDataTest() {
        CreateUserRequest admin = CreateUserRequest.getAdmin();

        new LoginPage()
                .open()
                .login(admin.getUsername(), admin.getPassword())
                .getPage(AdminPanel.class)
                .getAdminPanelText()
                .shouldBe(Condition.visible);
    }

    @Disabled("learning purpose")
    public void userCanLoginWithCorrectDataTest() {
        CreateUserRequest user = AdminStep.createUser();

        new LoginPage()
                .open()
                .login(user.getUsername(), user.getPassword())
                .getPage(UserDashboard.class)
                .getWelcomeText()
                .shouldBe(Condition.visible)
                .shouldHave(Condition.text("Welcome, noname!"));
    }
}