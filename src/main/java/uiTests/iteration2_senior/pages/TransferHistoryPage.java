package uiTests.iteration2_senior.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import common_iteration2.utils.MyRetryUtils;
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
        return MyRetryUtils.retry(
                () -> convertPageElement(historyText, TransactionType::new),
                result -> result != null && !result.isEmpty(),
                3,
                500);
    }

    public TransactionType findTransaction(String deposit) {
        return MyRetryUtils.retry(
                () -> getAllTransactions()
                        .stream()
                        .filter(t -> t.getAmount().equals(deposit))
                        .findFirst()
                        .orElse(null),
                result -> result != null,
                3 ,
                10000);
    }

    public void checkEmptyHistoryTransactions(){
        emptyHistory.shouldBe(Condition.empty);
    }
}