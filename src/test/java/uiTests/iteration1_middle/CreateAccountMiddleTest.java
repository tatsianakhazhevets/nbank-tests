package uiTests.iteration1_middle;

import apiTests.iteration1_senior.models.CreateAccountResponse;
import apiTests.iteration1_senior.steps.UserSteps;
import apiTests.iteration2_senior.models.CreateUserRequest;
import apiTests.iteration2_senior.steps.AdminStep;
import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.Test;
import uiTests.iteration1_middle.pages.BankAlert;
import uiTests.iteration1_middle.pages.UserDashboard;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountMiddleTest extends BaseUiTest {

    @Test
    public void userCanCreateAccountTest() {
        CreateUserRequest user = AdminStep.createUser();
        authAsUser(user);

        Selenide.open("/dashboard");

        new UserDashboard()
                .open()
                .createNewAccount();

        List<CreateAccountResponse> createdAccounts = new UserSteps(user.getUsername(), user.getPassword())
                .getAllAccounts();
        assertThat(createdAccounts).hasSize(1);

        new UserDashboard().checkAlertMessageAndAccept
                (BankAlert.NEW_ACCOUNT_CREATED.getMessage() + createdAccounts.get(0).getAccountNumber());
        assertThat(createdAccounts.get(0).getBalance()).isZero();
    }
}