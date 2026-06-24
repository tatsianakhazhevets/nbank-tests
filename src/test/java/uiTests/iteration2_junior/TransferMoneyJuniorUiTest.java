package uiTests.iteration2_junior;

import apiTests.iteration2_senior.generators.RandomModelGenerator;
import apiTests.iteration2_senior.models.*;
import apiTests.iteration2_senior.skelethon.endpoints.Endpoint;
import apiTests.iteration2_senior.skelethon.requests.CrudRequester;
import apiTests.iteration2_senior.specs.RequestSpecs;
import apiTests.iteration2_senior.specs.ResponseSpecs;
import apiTests.iteration2_senior.steps.AdminStep;
import apiTests.iteration2_senior.steps.DepositStep;
import apiTests.iteration2_senior.steps.UserStep;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static apiTests.iteration2_senior.models.TransactionType.TRANSFER_IN;
import static apiTests.iteration2_senior.models.TransactionType.TRANSFER_OUT;
import static apiTests.iteration2_senior.utils.RepeatUtil.repeat;
import static com.codeborne.selenide.Selenide.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class TransferMoneyJuniorUiTest {

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
    public void authorizedUserTransfersMoneySuccessfully() {
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
        CreateUserAccountResponse firstUserAccount = UserStep.createUserAccount(user);
        String firstAccountNumber = firstUserAccount.getAccountNumber();

        //Step 5: User deposits money into account1
        int deposit = 5000;
        repeat(2, () -> DepositStep.depositMoney(user, firstUserAccount, deposit));

        //Step 6: User creates account2
        CreateUserAccountResponse secondUserAccount = UserStep.createUserAccount(user);
        String secondAccountNumber = secondUserAccount.getAccountNumber();

        //TEST
        //Step 7: User transfers money (random value from 1 to 10000) from account1 to account2
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();
        $(Selectors.byText("-- Choose an account --")).click();
        $(Selectors.byText(firstAccountNumber)).click();
        $(Selectors.byAttribute("placeholder", "Enter recipient name"))
                .sendKeys(firstAccountNumber);
        $(Selectors.byAttribute("placeholder", "Enter recipient account number"))
                .sendKeys(secondAccountNumber);
        String transferMoneyRequest = Double.toString(RandomModelGenerator.generate(TransferMoneyRequest.class)
                .getAmount());
        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .sendKeys(transferMoneyRequest);
        $(Selectors.byId("confirmCheck")).setSelected(true).shouldBe(Condition.checked);
        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).click();

        //CHECK
        //Step 8: UI alert -> Successfully transferred $sum to account accountNumber! + check transfer is in the transactions history
        //Check alert
        Alert alert = switchTo().alert();
        String alertTextActualResult = alert.getText();
        String alertTextExpectedResult = "✅ Successfully transferred $" + transferMoneyRequest + " to account " + secondAccountNumber + "!";
        assertThat(alertTextActualResult).isEqualTo(alertTextExpectedResult);
        alert.accept();

        //Check that user stays on money transfer page after alert is accepted
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).shouldBe(Condition.visible);

        //Check deposit is in the transactions history
        $(Selectors.byText("\uD83C\uDFE0 Home")).click();
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();
        $(Selectors.byText("\uD83D\uDD01 Transfer Again")).click();
        List<String> historyText = $$("li.list-group-item span").texts();
        assertThat(historyText).anyMatch(t -> t.contains(TRANSFER_IN.getType() + " - $" + transferMoneyRequest));
        assertThat(historyText).anyMatch(t -> t.contains(TRANSFER_OUT.getType() + " - $" + transferMoneyRequest));

        //Step 9: Backend check: confirms the money was transferred, balance account1 = initial balance on account 1 - transfer amount,
        //balance account2 = initial balance on account 2 + transfer amount.
        AccountsNestedResponse[] existingUserAccounts = given()
                .spec(RequestSpecs.authUserSpec(user.getUsername(), user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat()
                .extract().as(AccountsNestedResponse[].class);
        assertThat(existingUserAccounts).isNotEmpty();

        List<TransactionNestedResponse> allUserTransactions = Arrays.asList(existingUserAccounts)
                .stream()
                .flatMap(tr -> tr.getTransactions().stream())
                .toList();

        TransactionNestedResponse userTransferInResponse = allUserTransactions
                .stream()
                .filter(t -> TRANSFER_IN.getType().equals(t.getType()))
                .findFirst()
                .orElseThrow();

        TransactionNestedResponse userTransferOutResponse = allUserTransactions
                .stream()
                .filter(t -> TRANSFER_OUT.getType().equals(t.getType()))
                .findFirst()
                .orElseThrow();

        assertThat(userTransferInResponse.getType()).isEqualTo(TRANSFER_IN.getType());
        assertThat(Double.toString(userTransferInResponse.getAmount())).isEqualTo(transferMoneyRequest);
        assertThat(userTransferOutResponse.getType()).isEqualTo(TRANSFER_OUT.getType());
        assertThat(Double.toString(userTransferOutResponse.getAmount())).isEqualTo(transferMoneyRequest);
    }

    @Test
    public void authorizedUserCannotTransferMoneyWithInvalidAmount() {
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
        CreateUserAccountResponse firstUserAccount = UserStep.createUserAccount(user);
        String firstAccountNumber = firstUserAccount.getAccountNumber();

        //Step 5: User deposits money into account1
        int deposit = 5000;
        repeat(2, () -> DepositStep.depositMoney(user, firstUserAccount, deposit));

        //Step 6: User creates account2
        CreateUserAccountResponse secondUserAccount = UserStep.createUserAccount(user);
        String secondAccountNumber = secondUserAccount.getAccountNumber();

        //TEST
        //Step 7: User attempts to transfer an invalid amount (e.g., 0, a negative number, or more than 10000).
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();
        $(Selectors.byText("-- Choose an account --")).click();
        $(Selectors.byText(firstAccountNumber)).click();
        $(Selectors.byAttribute("placeholder", "Enter recipient name"))
                .sendKeys(firstAccountNumber);
        $(Selectors.byAttribute("placeholder", "Enter recipient account number"))
                .sendKeys(secondAccountNumber);
        String transferMoneyRequest = "0";
        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .sendKeys(transferMoneyRequest);
        $(Selectors.byId("confirmCheck")).setSelected(true).shouldBe(Condition.checked);
        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).click();

        //CHECK
        //Step 8: UI alert ->  Error: Transfer amount must be ... + The transfer is NOT visible in the transaction history.
        Alert alert = switchTo().alert();
        String alertTextActualResult = alert.getText();
        String alertTextExpectedResult = "❌ Error: Transfer amount must be at least 0.01";
        assertThat(alertTextActualResult).isEqualTo(alertTextExpectedResult);
        alert.accept();

        //Check that user stays on money transfer page after alert is accepted
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).shouldBe(Condition.visible);

        // The transfer is NOT visible in the transaction history.
        $(Selectors.byText("\uD83C\uDFE0 Home")).click();
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();
        $(Selectors.byText("\uD83D\uDD01 Transfer Again")).click();
        List<String> historyText = $$("li.list-group-item span").texts();
        assertThat(historyText).noneMatch(t -> t.contains(TRANSFER_IN.getType() + " - $" + transferMoneyRequest));
        assertThat(historyText).noneMatch(t -> t.contains(TRANSFER_OUT.getType() + " - $" + transferMoneyRequest));

        //Step 9: Backend check: confirms the money was not transferred, balances on the accounts were not changed
        AccountsNestedResponse[] existingUserAccounts = given()
                .spec(RequestSpecs.authUserSpec(user.getUsername(), user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat()
                .extract().as(AccountsNestedResponse[].class);
        assertThat(existingUserAccounts).isNotEmpty();

        List<TransactionNestedResponse> allUserTransactions = Arrays.asList(existingUserAccounts)
                .stream()
                .flatMap(tr -> tr.getTransactions().stream())
                .toList();

        assertThat(allUserTransactions).noneMatch(t -> TRANSFER_IN.getType().equals(t.getType()));
        assertThat(allUserTransactions).noneMatch(t -> TRANSFER_OUT.getType().equals(t.getType()));
    }

    @Test
    public void authorizedUserCannotTransferMoneyWithMissingConfirmation() {
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
        CreateUserAccountResponse firstUserAccount = UserStep.createUserAccount(user);
        String firstAccountNumber = firstUserAccount.getAccountNumber();

        //Step 5: User deposits money into account1
        int deposit = 5000;
        repeat(2, () -> DepositStep.depositMoney(user, firstUserAccount, deposit));

        //Step 6: User creates account2
        CreateUserAccountResponse secondUserAccount = UserStep.createUserAccount(user);
        String secondAccountNumber = secondUserAccount.getAccountNumber();

        //TEST
        //Step 7:  User fills in transfer details (random value from 1 to 10000) but leaves the confirmation checkbox unchecked.
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();
        $(Selectors.byText("-- Choose an account --")).click();
        $(Selectors.byText(firstAccountNumber)).click();
        $(Selectors.byAttribute("placeholder", "Enter recipient name"))
                .sendKeys(firstAccountNumber);
        $(Selectors.byAttribute("placeholder", "Enter recipient account number"))
                .sendKeys(secondAccountNumber);
        String transferMoneyRequest = Double.toString(RandomModelGenerator.generate(TransferMoneyRequest.class)
                .getAmount());
        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .sendKeys(transferMoneyRequest);
        $(Selectors.byId("confirmCheck")).setSelected(false).shouldNotBe(Condition.checked);
        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).click();

        //CHECK
        //Step 8: UI alert -> Please fill all fields and confirm. + check transfer is not in the transactions history
        Alert alert = switchTo().alert();
        String alertTextActualResult = alert.getText();
        String alertTextExpectedResult = "❌ Please fill all fields and confirm.";
        assertThat(alertTextActualResult).isEqualTo(alertTextExpectedResult);
        alert.accept();

        //Check that user stays on money transfer page after alert is accepted
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).shouldBe(Condition.visible);

        //check transfer is not in the transactions history
        $(Selectors.byText("\uD83C\uDFE0 Home")).click();
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();
        $(Selectors.byText("\uD83D\uDD01 Transfer Again")).click();
        List<String> historyText = $$("li.list-group-item span").texts();
        assertThat(historyText).noneMatch(t -> t.contains(TRANSFER_IN.getType() + " - $" + transferMoneyRequest));
        assertThat(historyText).noneMatch(t -> t.contains(TRANSFER_OUT.getType() + " - $" + transferMoneyRequest));

        //Step 9: Backend check: confirms the money was not transferred, balances on the accounts were not changed
        AccountsNestedResponse[] existingUserAccounts = given()
                .spec(RequestSpecs.authUserSpec(user.getUsername(), user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat()
                .extract().as(AccountsNestedResponse[].class);
        assertThat(existingUserAccounts).isNotEmpty();

        List<TransactionNestedResponse> allUserTransactions = Arrays.asList(existingUserAccounts)
                .stream()
                .flatMap(tr -> tr.getTransactions().stream())
                .toList();

        assertThat(allUserTransactions).noneMatch(t -> TRANSFER_IN.getType().equals(t.getType()));
        assertThat(allUserTransactions).noneMatch(t -> TRANSFER_OUT.getType().equals(t.getType()));
    }
}