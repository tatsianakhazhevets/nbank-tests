package uiTests.iteration1_senior;

import apiTests.iteration1_senior.models.CreateAccountResponse;
import apiTests.iteration1_senior.steps.UserSteps;
import apiTests.iteration2_senior.models.CreateUserRequest;
import apiTests.iteration2_senior.steps.AdminStep;
import com.codeborne.selenide.Selenide;
import common.annotations.APIVersion;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import uiTests.iteration1_middle.pages.BankAlert;
import uiTests.iteration1_middle.pages.UserDashboard;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@APIVersion("with_validation_fix")
public class CreateAccountSeniorTest extends BaseUiTest {
    @Test
    @UserSession
    public void userCanCreateAccountTest() {
        Selenide.open("/dashboard");

        new UserDashboard()
                .open()
                .createNewAccount();

        List<CreateAccountResponse> createdAccounts = SessionStorage.getSteps()
                .getAllAccounts();
        assertThat(createdAccounts).hasSize(1);

        new UserDashboard().checkAlertMessageAndAccept
                (BankAlert.NEW_ACCOUNT_CREATED.getMessage() + createdAccounts.get(0).getAccountNumber());
        assertThat(createdAccounts.get(0).getBalance()).isZero();
    }
}