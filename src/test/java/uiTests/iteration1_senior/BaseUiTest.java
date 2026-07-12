package uiTests.iteration1_senior;

import apiTests.iteration1_senior.configs.Config;
import apiTests.iteration2_middle.BaseTest;
import apiTests.iteration2_senior.models.CreateUserRequest;
import apiTests.iteration2_senior.specs.RequestSpecs;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import common.extensions.AdminSessionExtension;
import common.extensions.BrowserMatchExtension;
import common.extensions.UserSessionExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import uiTests.iteration1_middle.pages.BasePage;

import java.util.Map;

import static com.codeborne.selenide.Selenide.executeJavaScript;

@ExtendWith(AdminSessionExtension.class) //автоматически доступен всем, кто наследуется от BaseUiTest
@ExtendWith(UserSessionExtension.class)
@ExtendWith(BrowserMatchExtension.class)
public class BaseUiTest extends BaseTest {
    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = Config.getProperty("uiRemote");
        Configuration.baseUrl = Config.getProperty("uIBaseUrl");
        Configuration.browser = Config.getProperty("browser");
        Configuration.browserSize = Config.getProperty("browserSize");

        Configuration.browserCapabilities.setCapability(
                "selenoid:options",
                Map.of(
                        "enableVNC", true,
                        "enableLog", true)
        );
    }
}