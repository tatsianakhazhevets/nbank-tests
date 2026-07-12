package uiTests.iteration1_middle.pages;

import apiTests.iteration1_senior.models.CreateUserRequest;
import apiTests.iteration2_senior.specs.RequestSpecs;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.Alert;
import uiTests.iteration1_middle.elements.BaseElement;

import java.util.List;
import java.util.function.Function;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class BasePage<T extends BasePage> {
    protected SelenideElement usernameInput = $(Selectors.byAttribute("placeholder", "Username"));
    protected SelenideElement passwordInput = $(Selectors.byAttribute("placeholder", "Password"));

    public abstract String url();

    public T open() {
        return Selenide.open(url(), (Class<T>) this.getClass());
    }

    public <T extends BasePage> T getPage(Class<T> pageClass) {
        return Selenide.page(pageClass);
    }

    public T checkAlertMessageAndAccept(String bankAlert) {
        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains(bankAlert);
        alert.accept();
        return (T) this;
    }

    public static void authAsUser(String username, String password) {
        Selenide.open("/login");
        String userAuthHeader = RequestSpecs.getUserAuthHeader(username, password);
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
    }

    // iteration1
    public static void authAsUser(apiTests.iteration1_senior.models.CreateUserRequest user) {
        authAsUser(user.getUsername(), user.getPassword());
    }

    // iteration2
    public static void authAsUser(apiTests.iteration2_senior.models.CreateUserRequest user) {
        authAsUser(user.getUsername(), user.getPassword());
    }

    //ElementCollection -> List<BaseElement>
    protected <T extends BaseElement> List<T> generatePageElements(ElementsCollection elementsCollection, Function<SelenideElement, T> constructor) {
        return elementsCollection.stream().map(constructor).toList();
    }
}
