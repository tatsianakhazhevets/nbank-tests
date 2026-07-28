package uiTests.iteration2_senior.elements;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;

public class MyBaseElement {
    protected final SelenideElement selenideElement;

    public MyBaseElement(SelenideElement selenideElement) {
        this.selenideElement = selenideElement;
    }

    protected SelenideElement find(By selector) {
        return selenideElement.find(selector);
    }

    protected SelenideElement find(String cssSelector) {
        return selenideElement.find(cssSelector);
    }

    protected ElementsCollection findAll(By selector) {
        return selenideElement.findAll(selector);
    }

    protected ElementsCollection findAll(String cssSelector) {
        return selenideElement.findAll(cssSelector);
    }
}