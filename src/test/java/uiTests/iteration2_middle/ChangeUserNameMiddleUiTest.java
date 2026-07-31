package uiTests.iteration2_middle;

import apiTests.iteration2_senior.generators.RandomModelGenerator;
import apiTests.iteration2_senior.models.ChangeUserNameRequest;
import apiTests.iteration2_senior.models.CreateUserRequest;
import apiTests.iteration2_senior.models.UserProfileNestedResponse;
import apiTests.iteration2_senior.skelethon.endpoints.Endpoint;
import apiTests.iteration2_senior.skelethon.requests.ValidatedCrudRequester;
import apiTests.iteration2_senior.specs.RequestSpecs;
import apiTests.iteration2_senior.specs.ResponseSpecs;
import apiTests.iteration2_senior.steps.AdminStep;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import uiTests.iteration2_middle.pages.AlertMessages;
import uiTests.iteration2_middle.pages.EditProfilePage;
import uiTests.iteration2_middle.pages.UserDashboard;

import static org.assertj.core.api.Assertions.assertThat;

public class ChangeUserNameMiddleUiTest extends BaseUiTest {

    @Disabled("learning purpose")
    public void authorizedUserCanChangeNameSuccessfully() {
        CreateUserRequest user = AdminStep.createUser();
        authAsUser(user);
        String changeUserNameRequest = RandomModelGenerator.generate(ChangeUserNameRequest.class).getName();

        new UserDashboard()
                .open()
                .chooseUserNameButton()
                .getPage(EditProfilePage.class)
                .changeUserName(changeUserNameRequest)
                .checkAlertMessageAndAccept(AlertMessages.NAME_UPDATED_SUCCESSFULLY.getMessage())
                .checkUserStaysOnEditProfilePage()
                .returnToHomePage()
                .getPage(UserDashboard.class)
                .checkUserName(changeUserNameRequest);

        UserProfileNestedResponse existingUserProfile = new ValidatedCrudRequester<UserProfileNestedResponse>(
                RequestSpecs.authUserSpec(user.getUsername(), user.getPassword()),
                Endpoint.CUSTOMER_PROFILE_GET,
                ResponseSpecs.requestReturnsOk())
                .get();

        assertThat(existingUserProfile.getName()).isEqualTo(changeUserNameRequest);
    }

    @Disabled("learning purpose")
    public void changeNameInvalidCases() {
        CreateUserRequest user = AdminStep.createUser();
        authAsUser(user);
        String changeUserNameRequest = RandomModelGenerator.generate(ChangeUserNameRequest.class).getName()
                .replaceAll("\\s+", "");

        new UserDashboard()
                .open()
                .chooseUserNameButton()
                .getPage(EditProfilePage.class)
                .changeUserName(changeUserNameRequest)
                .checkAlertMessageAndAccept(AlertMessages.NAME_MUST_CONTAIN_TWO_WORDS_WITH_LETTERS_ONLY.getMessage())
                .checkUserStaysOnEditProfilePage()
                .returnToHomePage()
                .getPage(UserDashboard.class)
                .redirectToUserDashboard();

        UserProfileNestedResponse existingUserProfile = new ValidatedCrudRequester<UserProfileNestedResponse>(
                RequestSpecs.authUserSpec(user.getUsername(), user.getPassword()),
                Endpoint.CUSTOMER_PROFILE_GET,
                ResponseSpecs.requestReturnsOk())
                .get();

        assertThat(existingUserProfile.getName()).isNull();
    }
}