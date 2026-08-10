package uiTests.iteration2_senior.pages;

import com.codeborne.selenide.*;
import common_iteration2.utils.MyRetryUtils;
import uiTests.iteration2_senior.pages.BasePage;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class DepositPage extends BasePage<DepositPage> {

    private SelenideElement enterAmountField = $(Selectors.byAttribute("placeholder", "Enter amount"));
    private SelenideElement depositButton = $(Selectors.byText("💵 Deposit"));
    private SelenideElement depositPage = $(Selectors.byText("\uD83D\uDCB0 Deposit Money"));
    private ElementsCollection chosenOption = $$("option");
    private SelenideElement accountOption = $(".account-selector");

    @Override
    public String url() {
        return "/deposit";
    }

    public DepositPage chooseAccount(String accountNumber) {
        MyRetryUtils.retry(
                () -> {
                    accountOption.selectOptionContainingText(accountNumber);
                    return accountOption.getSelectedOptionText();
                },
                selectedText -> selectedText != null && selectedText.contains(accountNumber),
                3,
                10000
        );
        return this;
    }

    public DepositPage chooseAnAccountOptionWithoutCreatedAccount() {
        chosenOption.shouldHave(CollectionCondition.size(1));
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