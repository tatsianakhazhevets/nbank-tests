package ui.junior;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import apiTests.iteration1_senior.models.CreateAccountResponse;
import apiTests.iteration1_senior.models.LoginUserRequest;
import apiTests.iteration1_senior.skelethon.Endpoint;
import apiTests.iteration1_senior.skelethon.requesters.CrudRequester;
import apiTests.iteration1_senior.specs.RequestSpecs;
import apiTests.iteration1_senior.specs.ResponseSpecs;
import apiTests.iteration2_senior.models.CreateUserRequest;
import apiTests.iteration2_senior.steps.AdminStep;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codeborne.selenide.Selenide.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest {

    @BeforeAll
    public static void setupSelenoid() {
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
    public void userCanCreateAccountTest() {
        //ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        //ШАГ 1: Админ логинится в банке
        //ШАГ 2: Админ создает юзера
        //ШАГ 3: Юзер логинится в банке

        CreateUserRequest user = AdminStep.createUser();

        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnOK())
                .post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract()
                .header("Authorization");

        Selenide.open("/login");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
        Selenide.open("/dashboard");

        //ШАГ ТЕСТА
        //ШАГ 4: Юзер создает аккаунт
        $(Selectors.byText("➕ Create New Account")).click();

        //ШАГ 5: Проверка, что аккаунт был создан на UI
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("✅ New Account Created! Account Number: ");
        alert.accept();

        Pattern pattern = Pattern.compile("Account Number: (\\w+)");
        Matcher matcher = pattern.matcher(alertText);
        matcher.find();
        String createAccNumber = matcher.group(1);

        //ШАГ 6: Проверка, что аккаунт был создан на API
        CreateAccountResponse[]  existingUserAccounts = given()
                .spec(RequestSpecs.authAsUser(user.getUsername(), user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat()
                .extract().as(CreateAccountResponse[].class);

        assertThat(existingUserAccounts).hasSize(1);

        CreateAccountResponse createdAccount = existingUserAccounts[0];

        assertThat(createdAccount).isNotNull();
        assertThat(createdAccount.getBalance()).isZero();

    }




}