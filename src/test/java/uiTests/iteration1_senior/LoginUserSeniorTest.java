package uiTests.iteration1_senior;

import apiTests.iteration2_senior.models.CreateUserRequest;
import apiTests.iteration2_senior.steps.AdminStep;
import com.codeborne.selenide.Condition;
import common.annotations.APIVersion;
import common.annotations.Browsers;
import org.junit.jupiter.api.Test;
import uiTests.iteration1_middle.pages.AdminPanel;
import uiTests.iteration1_middle.pages.LoginPage;
import uiTests.iteration1_middle.pages.UserDashboard;

@APIVersion("with_validation_fix")
public class LoginUserSeniorTest extends BaseUiTest {
    @Test
    @Browsers({"chrome"})
    public void adminCanLoginWithCorrectDataTest() {
        CreateUserRequest admin = CreateUserRequest.getAdmin();

        new LoginPage()
                .open()
                .login(admin.getUsername(), admin.getPassword())
                .getPage(AdminPanel.class)
                .getAdminPanelText()
                .shouldBe(Condition.visible);
    }

    @Test
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