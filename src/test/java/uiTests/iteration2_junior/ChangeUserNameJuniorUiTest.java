package uiTests.iteration2_junior;

import apiTests.iteration2_senior.generators.RandomModelGenerator;
import apiTests.iteration2_senior.models.*;
import apiTests.iteration2_senior.skelethon.endpoints.Endpoint;
import apiTests.iteration2_senior.skelethon.requests.CrudRequester;
import apiTests.iteration2_senior.specs.RequestSpecs;
import apiTests.iteration2_senior.specs.ResponseSpecs;
import apiTests.iteration2_senior.steps.AdminStep;
import com.codeborne.selenide.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;

import java.util.Map;

import static com.codeborne.selenide.Selenide.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class ChangeUserNameJuniorUiTest {
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
    public void authorizedUserCanChangeNameSuccessfully() {
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
        //Step 4: User changes the name (valid values)
        $(Selectors.byClassName("user-name")).click();
        String changeUserNameRequest = RandomModelGenerator.generate(ChangeUserNameRequest.class).getName();
        SelenideElement input = $("input[placeholder='Enter new name']").setValue(changeUserNameRequest);
        //Without the second input, the test doesn't work, I assume it's a bug:
        input.setValue(changeUserNameRequest);
        input.shouldHave(Condition.value(changeUserNameRequest));
        $(Selectors.byText("\uD83D\uDCBE Save Changes")).click();

        //CHECK
        //Step 5: UI alert -> Name updated successfully! + check User Dashboard Welcome, new name!
        //Check alert
        Alert alert = switchTo().alert();
        String alertTextActualResult = alert.getText();
        String alertTextExpectedResult = "✅ Name updated successfully!";
        assertThat(alertTextActualResult).isEqualTo(alertTextExpectedResult);
        alert.accept();

        //Check that user stays on  page after alert is accepted
        $(Selectors.byText("✏\uFE0F Edit Profile")).shouldBe(Condition.visible);

        //check User Dashboard Welcome, new name!
        $(Selectors.byText("\uD83C\uDFE0 Home")).click();
        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible)
                .shouldHave(Condition.text("Welcome, " + changeUserNameRequest + "!"));

        //Step 6: Backend check: confirms the name was updated.
        UserProfileNestedResponse existingUserProfile = given()
                .spec(RequestSpecs.authUserSpec(user.getUsername(), user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/profile")
                .then().assertThat()
                .extract().as(UserProfileNestedResponse.class);

        assertThat(existingUserProfile.getName()).isEqualTo(changeUserNameRequest);
    }

    @Test
    public void changeNameInvalidCases() {
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
        //Step 4: User changes the name (invalid values)
        $(Selectors.byClassName("user-name")).click();
        String changeUserNameRequest = RandomModelGenerator.generate(ChangeUserNameRequest.class).getName()
                .replaceAll("\\s+", "");
        SelenideElement input = $("input[placeholder='Enter new name']").setValue(changeUserNameRequest);
        //Without the second input, the test doesn't work, I assume it's a bug:
        input.setValue(changeUserNameRequest);
        input.shouldHave(Condition.value(changeUserNameRequest));
        $(Selectors.byText("\uD83D\uDCBE Save Changes")).click();

        //CHECK
        //Step 5: UI alert -> Name updated successfully! + check User Dashboard Welcome, new name!
        //Check alert
        Alert alert = switchTo().alert();
        String alertTextActualResult = alert.getText();
        String alertTextExpectedResult = "Name must contain two words with letters only";
        assertThat(alertTextActualResult).isEqualTo(alertTextExpectedResult);
        alert.accept();

        //Check that user stays on  page after alert is accepted
        $(Selectors.byText("✏\uFE0F Edit Profile")).shouldBe(Condition.visible);

        //check User Dashboard Welcome, new name!
        $(Selectors.byText("\uD83C\uDFE0 Home")).click();
        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible)
                .shouldHave(Condition.text("Welcome, noname!"));

        //Step 6: Backend check: confirms the name was updated.
        UserProfileNestedResponse existingUserProfile = given()
                .spec(RequestSpecs.authUserSpec(user.getUsername(), user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/profile")
                .then().assertThat()
                .extract().as(UserProfileNestedResponse.class);

        assertThat(existingUserProfile.getName()).isNull();
    }
}