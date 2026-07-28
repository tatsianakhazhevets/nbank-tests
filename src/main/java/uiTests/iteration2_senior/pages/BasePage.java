package uiTests.iteration2_senior.pages;

import apiTests.iteration2_senior.models.CreateUserRequest;
import apiTests.iteration2_senior.specs.RequestSpecs;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.Alert;
import uiTests.iteration2_senior.elements.MyBaseElement;

import java.util.List;
import java.util.function.Function;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class BasePage<T extends BasePage> {

    protected SelenideElement homeButton = $(Selectors.byText("\uD83C\uDFE0 Home"));

    public abstract String url();

    public T open() {
        return Selenide.open(url(), (Class<T>) this.getClass());
    }

    public <T extends BasePage> T getPage(Class<T> pageClass) {
        return Selenide.page(pageClass);
    }

    public T checkAlertMessageAndAccept(String alertMessages) {
        Alert alert = switchTo().alert();
        assertThat(alert.getText()).isEqualTo(alertMessages);
        alert.accept();
        return (T) this;
    }

    public static void authAsUser(String username, String password) {
        Selenide.open("/login");
        String userAuthHeader = RequestSpecs.getUserAuthHeader(username, password);
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
    }

    public static void authAsUser(CreateUserRequest createUserRequest) {
        authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword());
    }

    //ElementCollection -> List<BaseElement>
    protected <T extends MyBaseElement> List<T> convertPageElement(ElementsCollection elementsCollection,
                                                                   Function<SelenideElement, T> constructor) {
        return elementsCollection.stream().map(constructor).toList();
    }
}