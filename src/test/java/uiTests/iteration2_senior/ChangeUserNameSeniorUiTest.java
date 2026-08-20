package uiTests.iteration2_senior;

import apiTests.iteration2_senior.generators.RandomModelGenerator;
import apiTests.iteration2_senior.models.ChangeUserNameRequest;
import apiTests.iteration2_senior.models.CreateUserRequest;
import apiTests.iteration2_senior.models.UserProfileNestedResponse;
import apiTests.iteration2_senior.skelethon.endpoints.Endpoint;
import apiTests.iteration2_senior.skelethon.requests.ValidatedCrudRequester;
import apiTests.iteration2_senior.specs.RequestSpecs;
import apiTests.iteration2_senior.specs.ResponseSpecs;
import common.annotations.APIVersion;
import common_iteration2.annotations.MyUserSession;
import common_iteration2.storage.MySessionStorage;
import org.junit.jupiter.api.Test;
import uiTests.iteration2_senior.pages.AlertMessages;
import uiTests.iteration2_senior.pages.EditProfilePage;
import uiTests.iteration2_senior.pages.UserDashboard;

import static org.assertj.core.api.Assertions.assertThat;

@APIVersion("with_validation_fix")
public class ChangeUserNameSeniorUiTest extends BaseUiSeniorTest {
    @Test
    @MyUserSession
    public void authorizedUserCanChangeNameSuccessfully() {
        //1. Test data
        CreateUserRequest user = MySessionStorage.getUserFromStorage();
        String changeUserNameRequest = RandomModelGenerator.generate(ChangeUserNameRequest.class).getName();

        //2. Test steps
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

        //3. Test Results
        UserProfileNestedResponse existingUserProfile = new ValidatedCrudRequester<UserProfileNestedResponse>(
                RequestSpecs.authUserSpec(user.getUsername(), user.getPassword()),
                Endpoint.CUSTOMER_PROFILE_GET,
                ResponseSpecs.requestReturnsOk())
                .get();

        assertThat(existingUserProfile.getName()).isEqualTo(changeUserNameRequest);
    }

    @Test
    @MyUserSession
    public void changeNameInvalidCases() {
        //1. Test data
        CreateUserRequest user = MySessionStorage.getUserFromStorage();
        String changeUserNameRequest = RandomModelGenerator.generate(ChangeUserNameRequest.class).getName()
                .split(" ")[0];

        //2. Test steps
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

        //3. Test Results
        UserProfileNestedResponse existingUserProfile = new ValidatedCrudRequester<UserProfileNestedResponse>(
                RequestSpecs.authUserSpec(user.getUsername(), user.getPassword()),
                Endpoint.CUSTOMER_PROFILE_GET,
                ResponseSpecs.requestReturnsOk())
                .get();

        assertThat(existingUserProfile.getName()).isNull();
    }
}