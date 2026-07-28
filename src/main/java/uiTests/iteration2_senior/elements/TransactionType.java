package uiTests.iteration2_senior.elements;

import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

@Getter
public class TransactionType extends MyBaseElement {
    private String type;
    private String amount;

    public TransactionType(SelenideElement selenideElement) {
        super(selenideElement);
        String text = selenideElement.getText().split("\n")[0];
        String[] parts = text.split(" - ");

        type = parts[0];
        amount = parts[1].replace("$", "");
    }
}