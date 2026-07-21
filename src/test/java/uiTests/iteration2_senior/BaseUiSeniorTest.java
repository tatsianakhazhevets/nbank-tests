package uiTests.iteration2_senior;

import apiTests.iteration2_senior.BaseTest;
import apiTests.iteration2_senior.configs.Config;
import com.codeborne.selenide.Configuration;
import common_iteration2.extensions.MyUserSessionExtension;
import common_iteration2.extensions.UserAccountExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

@ExtendWith(MyUserSessionExtension.class)
@ExtendWith(UserAccountExtension.class)
public class BaseUiSeniorTest extends BaseTest {
    @BeforeAll
    public static void setUp() {
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