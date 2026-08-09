package uiTests.iteration2_senior.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import common_iteration2.utils.MyRetryUtils;
import uiTests.iteration2_senior.pages.BasePage;

import static com.codeborne.selenide.Selenide.$;

public class EditProfilePage extends BasePage<EditProfilePage> {

    private SelenideElement enterNewNameField = $("input[placeholder='Enter new name']");
    private SelenideElement saveChangesButton = $(Selectors.byText("\uD83D\uDCBE Save Changes"));
    private SelenideElement editProfilePage = $(Selectors.byText("✏\uFE0F Edit Profile"));

    @Override
    public String url() {
        return "/edit-profile";
    }

    public EditProfilePage changeUserName(String userName) {
        enterNewNameField.setValue(userName);
        enterNewNameField
                .shouldBe(Condition.visible)
                .setValue(userName)
                .shouldHave(Condition.value(userName));

        saveChangesButton.click();
        return this;
    }

    public EditProfilePage checkUserStaysOnEditProfilePage() {
        editProfilePage.shouldBe(Condition.visible);
        return this;
    }

    public EditProfilePage returnToHomePage() {
        homeButton.click();
        return this;
    }
}