package uiTests.iteration2_middle.pages;

import com.codeborne.selenide.*;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class DepositPage extends BasePage<DepositPage> {

    private SelenideElement chooseAnAccountOption = $(Selectors.byText("-- Choose an account --"));
    private SelenideElement enterAmountField = $(Selectors.byAttribute("placeholder", "Enter amount"));
    private SelenideElement depositButton = $(Selectors.byText("💵 Deposit"));
    private SelenideElement depositPage = $(Selectors.byText("\uD83D\uDCB0 Deposit Money"));
    private ElementsCollection chosenOption = $$("option");

    @Override
    public String url() {
        return "/deposit";
    }

    public DepositPage chooseAnAccountOption() {
        chooseAnAccountOption.click();
        return this;
    }

    public DepositPage chooseAnAccountOptionWithoutCreatedAccount() {
        chosenOption.shouldHave(CollectionCondition.size(1));
        return this;
    }

    public DepositPage chooseAnAccountNumberOption(String accountNumber) {
        $(Selectors.byText(accountNumber)).click();
        return this;
    }

    public DepositPage enterAmountAndClickDepositButton(String deposit) {
        enterAmountField.sendKeys(deposit);
        depositButton.click();
        return this;
    }

    public DepositPage checkUserStaysOnDepositPage() {
        depositPage.shouldBe(Condition.visible);
        return this;
    }

    public DepositPage returnToHomePage() {
        homeButton.click();
        return this;
    }
}