package uiTests.iteration2_senior.pages;

import com.codeborne.selenide.*;
import common_iteration2.utils.MyRetryUtils;
import uiTests.iteration2_senior.pages.BasePage;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class TransferPage extends BasePage<TransferPage> {

    private SelenideElement transferAgainButton = $(Selectors.byText("\uD83D\uDD01 Transfer Again"));
    private SelenideElement recipientName = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
    private SelenideElement recipientAccountNumber = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
    private SelenideElement enterAmountField = $(Selectors.byAttribute("placeholder", "Enter amount"));
    private SelenideElement checkBox = $(Selectors.byId("confirmCheck"));
    private SelenideElement sendTransferButton = $(Selectors.byText("\uD83D\uDE80 Send Transfer"));
    private SelenideElement transferPage = $(Selectors.byText("\uD83D\uDD04 Make a Transfer"));
    private SelenideElement accountOption = $(".account-selector");

    @Override
    public String url() {
        return "/transfer";
    }

    public TransferPage chooseTransferAgainButton() {
        transferAgainButton.shouldBe(Condition.visible).click();
        return this;
    }

    public TransferPage chooseAccount(String accountNumber) {
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

    public TransferPage enterRecipientName(String accountNumber) {
        recipientName.sendKeys(accountNumber);
        return this;
    }

    public TransferPage enterRecipientAccountNumber(String accountNumber) {
        recipientAccountNumber.sendKeys(accountNumber);
        return this;
    }

    public TransferPage enterAmount(String transfer) {
        enterAmountField.sendKeys(transfer);
        return this;
    }

    public TransferPage enableCheckBox() {
        checkBox.setSelected(true).shouldBe(Condition.checked);
        return this;
    }

    public TransferPage disableCheckBox() {
        checkBox.setSelected(false).shouldNotBe(Condition.checked);
        return this;
    }

    public TransferPage clickSendTransferButton() {
        sendTransferButton.shouldBe(Condition.visible).click();
        return this;
    }

    public TransferPage checkUserStaysOnTransferPage() {
        transferPage.shouldBe(Condition.visible);
        return this;
    }

    public TransferPage returnToHomePage() {
        homeButton.shouldBe(Condition.visible).click();
        return this;
    }
}