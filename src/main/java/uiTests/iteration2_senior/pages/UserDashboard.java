package uiTests.iteration2_senior.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import uiTests.iteration2_senior.pages.BasePage;

import static com.codeborne.selenide.Selenide.$;

public class UserDashboard extends BasePage<UserDashboard> {

    private SelenideElement welcomeText = $(Selectors.byClassName("welcome-text"));
    private SelenideElement depositOptionButton = $(Selectors.byText("\uD83D\uDCB0 Deposit Money"));
    private SelenideElement transferOptionButton = $(Selectors.byText("\uD83D\uDD04 Make a Transfer"));
    private SelenideElement userNameButton = $(Selectors.byClassName("user-name"));

    @Override
    public String url() {
        return "/dashboard";
    }

    public UserDashboard redirectToUserDashboard() {
        welcomeText.shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));
        return this;
    }

    public UserDashboard chooseDepositButton() {
        depositOptionButton.click();
        return this;
    }

    public UserDashboard chooseTransferButton() {
        transferOptionButton.click();
        return this;
    }

    public UserDashboard chooseUserNameButton() {
        userNameButton.click();
        return this;
    }

    public UserDashboard checkUserName(String userName) {
        welcomeText.shouldBe(Condition.visible)
                .shouldHave(Condition.text("Welcome, " + userName + "!"));
        return this;
    }
}