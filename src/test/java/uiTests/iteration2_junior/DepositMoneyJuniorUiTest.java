package uiTests.iteration2_junior;

import apiTests.iteration2_senior.generators.RandomModelGenerator;
import apiTests.iteration2_senior.models.*;
import apiTests.iteration2_senior.skelethon.endpoints.Endpoint;
import apiTests.iteration2_senior.skelethon.requests.CrudRequester;
import apiTests.iteration2_senior.specs.RequestSpecs;
import apiTests.iteration2_senior.specs.ResponseSpecs;
import apiTests.iteration2_senior.steps.AdminStep;
import apiTests.iteration2_senior.steps.UserStep;
import com.codeborne.selenide.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;

import java.util.Map;

import static apiTests.iteration2_senior.models.TransactionType.DEPOSIT;
import static com.codeborne.selenide.Selenide.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class DepositMoneyJuniorUiTest {

    @BeforeAll
    public static void setUp() {
        Configuration.remote = "http://localhost:4444/wd/hub";
        Configuration.baseUrl = "http://172.28.64.1:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";

        Configuration.browserCapabilities.setCapability(
                "selenoid:options",
                Map.of(
                        "enableVNC", true,
                        "enableLog", true)
        );
    }

    @Test
    public void authorizedUserDepositsMoneySuccessfully() {
        //PRECONDITIONS
        //Step 1: Admin logs in
        //Step 2: Admin creates user
        CreateUserRequest user = AdminStep.createUser();

        //Step 3: User logs in
        String userAuthHeader = new CrudRequester(
                RequestSpecs.unAuthSpec(),
                Endpoint.LOGIN_POST,
                ResponseSpecs.requestReturnsOk())
                .post(LoginUserRequest.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .build())
                .extract()
                .header("Authorization");

        Selenide.open("/login");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
        Selenide.open("/dashboard");

        //Step 4: User creates account
        CreateUserAccountResponse userAccount = UserStep.createUserAccount(user);
        String accountNumber = userAccount.getAccountNumber();

        //TEST
        //Step 5: User deposits money (random value from 1 to 5000)
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
        $(Selectors.byText("-- Choose an account --")).click();
        $(Selectors.byText(accountNumber)).click();
        String depositMoneyRequest = Double.toString(RandomModelGenerator.generate(DepositMoneyRequest.class)
                .getBalance());
        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .sendKeys(depositMoneyRequest);
        $(Selectors.byText("💵 Deposit")).click();

        //CHECK
        //Step 6: UI alert -> Successfully deposited $sum to account accountName! + check deposit is in the transactions history
        //Check alert
        Alert alert = switchTo().alert();
        String alertTextActualResult = alert.getText();
        String alertTextExpectedResult = "✅ Successfully deposited $" + depositMoneyRequest + " to account " + accountNumber + "!";
        assertThat(alertTextActualResult).isEqualTo(alertTextExpectedResult);
        alert.accept();

        //Check redirection to dashboard page after alert is accepted
        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible)
                .shouldHave(Condition.text("Welcome, noname!"));

        //Check deposit is in the transactions history
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();
        $(Selectors.byText("\uD83D\uDD01 Transfer Again")).click();
        String historyText = $$("li.list-group-item span")
                .findBy(Condition.text(depositMoneyRequest))
                .getText();
        String type = historyText.split(" - ")[0];
        String amountElement = historyText.split(" - ")[1].split("\n")[0];
        String amount = amountElement.replace("$", "");
        assertThat(type).isEqualTo(DEPOSIT.getType());
        assertThat(amount).isEqualTo(depositMoneyRequest);

        //Step 7: Backend check: confirms the money was deposited.
        AccountsNestedResponse[] existingUserAccounts = given()
                .spec(RequestSpecs.authUserSpec(user.getUsername(), user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat()
                .extract().as(AccountsNestedResponse[].class);
        assertThat(existingUserAccounts).isNotEmpty();

        AccountsNestedResponse userAccountsResponse = existingUserAccounts[0];
        TransactionNestedResponse userDepositResponse = userAccountsResponse.getTransactions()
                .stream()
                .filter(t -> DEPOSIT.getType().equals(t.getType()))
                .findFirst()
                .orElseThrow();
        assertThat(Double.toString(userDepositResponse.getAmount())).isEqualTo(depositMoneyRequest);
        assertThat(userDepositResponse.getType()).isEqualTo(DEPOSIT.getType());
    }

    @Test
    public void authorizedUserCannotDepositInvalidAmount() {
        //PRECONDITIONS
        //Step 1: Admin logs in
        //Step 2: Admin creates user
        CreateUserRequest user = AdminStep.createUser();

        //Step 3: User logs in
        String userAuthHeader = new CrudRequester(
                RequestSpecs.unAuthSpec(),
                Endpoint.LOGIN_POST,
                ResponseSpecs.requestReturnsOk())
                .post(LoginUserRequest.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .build())
                .extract()
                .header("Authorization");

        Selenide.open("/login");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
        Selenide.open("/dashboard");

        //Step 4: User creates account
        CreateUserAccountResponse userAccount = UserStep.createUserAccount(user);
        String accountNumber = userAccount.getAccountNumber();

        //TEST
        //Step 5:User attempts to deposit an invalid amount (random 0, negative and greater than 5000)
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
        $(Selectors.byText("-- Choose an account --")).click();
        $(Selectors.byText(accountNumber)).click();
        String depositMoneyRequest = Double.toString(50001);
        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .sendKeys(depositMoneyRequest);
        $(Selectors.byText("💵 Deposit")).click();

        //CHECK
        //Step 6: UI alert -> Please deposit less or equal to 5000$. + check deposit is not in the transactions history
        //Check alert
        Alert alert = switchTo().alert();
        String alertTextActualResult = alert.getText();
        assertThat(alertTextActualResult).isEqualTo("❌ Please deposit less or equal to 5000$.");
        alert.accept();

        //Check that user stays on money deposit page after alert is accepted
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).shouldBe(Condition.visible);

        //check deposit is not in the transactions history
        $(Selectors.byText("\uD83C\uDFE0 Home")).click();
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();
        $(Selectors.byText("\uD83D\uDD01 Transfer Again")).click();
        $(("ul.list-group")).shouldBe(Condition.empty);

        //Step 7: Backend check: confirms the money was not deposited
        AccountsNestedResponse[] existingUserAccounts = given()
                .spec(RequestSpecs.authUserSpec(user.getUsername(), user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat()
                .extract().as(AccountsNestedResponse[].class);
        assertThat(existingUserAccounts).isNotEmpty();

        AccountsNestedResponse userAccountsResponse = existingUserAccounts[0];
        int userDepositResponse = userAccountsResponse.getTransactions().size();
        assertThat(userDepositResponse).isEqualTo(0);
    }

    @Test
    public void authorizedUserCannotDepositToNonExistingAccount() {
        //PRECONDITIONS
        //Step 1: Admin logs in
        //Step 2: Admin creates user
        CreateUserRequest user = AdminStep.createUser();

        //Step 3: User logs in
        String userAuthHeader = new CrudRequester(
                RequestSpecs.unAuthSpec(),
                Endpoint.LOGIN_POST,
                ResponseSpecs.requestReturnsOk())
                .post(LoginUserRequest.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .build())
                .extract()
                .header("Authorization");

        Selenide.open("/login");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
        Selenide.open("/dashboard");

        //TEST
        //Step 4: User attempts to deposit money (random value from 1 to 5000) without selecting an account.
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
        $(Selectors.byText("-- Choose an account --")).click();
        $$("option").shouldHave(CollectionCondition.size(1));
        String depositMoneyRequest = Double.toString(RandomModelGenerator.generate(DepositMoneyRequest.class)
                .getBalance());
        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .sendKeys(depositMoneyRequest);
        $(Selectors.byText("💵 Deposit")).click();

        //CHECK
        //Step 5: UI alert ->  Please select an account. + check deposit is not in the transactions history
        //Check alert
        Alert alert = switchTo().alert();
        String alertTextActualResult = alert.getText();
        assertThat(alertTextActualResult).isEqualTo("❌ Please select an account.");
        alert.accept();

        //Check that user stays on money deposit page after alert is accepted
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).shouldBe(Condition.visible);

        //check deposit is not in the transactions history
        $(Selectors.byText("\uD83C\uDFE0 Home")).click();
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();
        $(Selectors.byText("\uD83D\uDD01 Transfer Again")).click();
        $(("ul.list-group")).shouldBe(Condition.empty);

        //Step 6: Backend check: confirms the money was not deposited.
        AccountsNestedResponse[] existingUserAccounts = given()
                .spec(RequestSpecs.authUserSpec(user.getUsername(), user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat()
                .extract().as(AccountsNestedResponse[].class);
        assertThat(existingUserAccounts).isEmpty();
    }
}