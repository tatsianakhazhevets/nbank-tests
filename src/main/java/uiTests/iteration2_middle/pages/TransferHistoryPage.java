package uiTests.iteration2_middle.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import uiTests.iteration2_middle.models.MatchingTransactionsUI;

import java.util.List;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class TransferHistoryPage extends BasePage<TransferHistoryPage> {

    private ElementsCollection historyText = $$("li.list-group-item span");
    private SelenideElement emptyHistory = $(("ul.list-group"));

    @Override
    public String url() {
        return "/transfer";
    }

    public MatchingTransactionsUI checkMatchingDepositTransactions(String deposit) {
        String depositHistory = historyText.findBy(Condition.text(deposit)).getText();
        String type = depositHistory.split(" - ")[0];
        String amountElement = depositHistory.split(" - ")[1].split("\n")[0];
        String amount = amountElement.replace("$", "");
        return new MatchingTransactionsUI(type, amount);
    }

    public List<String> getHistoryText() {
        return historyText.texts();
    }

    public MatchingTransactionsUI checkEmptyHistoryTransactions() {
        emptyHistory.shouldBe(Condition.empty);
        return new MatchingTransactionsUI();
    }
}