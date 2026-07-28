package uiTests.iteration2_senior.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import uiTests.iteration2_senior.elements.TransactionType;
//import uiTests.iteration2_middle.models.MatchingTransactionsUI;
import uiTests.iteration2_senior.pages.BasePage;

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

    public List<TransactionType> getAllTransactions() {
        return convertPageElement(historyText, TransactionType::new);
    }

    public TransactionType findTransaction(String deposit) {
        return getAllTransactions()
                .stream()
                .filter(t -> t.getAmount().equals(deposit))
                .findFirst()
                .orElseThrow();
    }

    public void checkEmptyHistoryTransactions(){
        emptyHistory.shouldBe(Condition.empty);
    }
}