package iteration2_senior;

import iteration2_senior.assertions.AssertingClass;
import iteration2_senior.generators.RandomModelGenerator;
import iteration2_senior.models.ChangeUserNameRequest;
import iteration2_senior.models.ChangeUserNameResponse;
import iteration2_senior.models.CreateUserRequest;
import iteration2_senior.models.UserProfileNestedResponse;
import iteration2_senior.skelethon.endpoints.Endpoint;
import iteration2_senior.skelethon.requests.CrudRequester;
import iteration2_senior.skelethon.requests.ValidatedCrudRequester;
import iteration2_senior.specs.RequestSpecs;
import iteration2_senior.specs.ResponseSpecs;
import iteration2_senior.steps.AdminStep;
import iteration2_senior.steps.UserStep;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static iteration2_senior.specs.Messages.NAME_MUST_CONTAINS_TWO_WORDS;
import static iteration2_senior.specs.Messages.PROFILE_UPDATED_SUCCESSFULLY;

public class ChangeUserNameSeniorTest extends BaseTest {

    @Test
    public void authorizedUserCanChangeNameSuccessfully() {
        CreateUserRequest user = AdminStep.createUser();
        UserStep.login(user);
        ChangeUserNameRequest changeUserNameRequest = RandomModelGenerator.generate(ChangeUserNameRequest.class);

        // Authorized user can change their name (T29_Positive test)
        ChangeUserNameResponse changeUserNameResponse = new ValidatedCrudRequester<ChangeUserNameResponse>(
                RequestSpecs.authUserSpec(user.getUsername(), user.getPassword()),
                Endpoint.CUSTOMER_PROFILE_UPDATE,
                ResponseSpecs.requestReturnsOk())
                .put(changeUserNameRequest);

        UserProfileNestedResponse changedUserName = new CrudRequester(
                RequestSpecs.authUserSpec(user.getUsername(), user.getPassword()),
                Endpoint.CUSTOMER_PROFILE_GET,
                ResponseSpecs.requestReturnsOk())
                .get()
                .extract()
                .as(UserProfileNestedResponse.class);

        AssertingClass.assertThat(changeUserNameRequest, changeUserNameResponse).match();
        softly.assertThat(changedUserName.getName()).isEqualTo(changeUserNameRequest.getName());
        softly.assertThat(changeUserNameResponse.getMessage()).isEqualTo(PROFILE_UPDATED_SUCCESSFULLY.getMessage());


        // Authorized third user can change their name on the same name(T33_Positive test)
        ChangeUserNameResponse changeUserNameSecondResponse = new ValidatedCrudRequester<ChangeUserNameResponse>(
                RequestSpecs.authUserSpec(user.getUsername(), user.getPassword()),
                Endpoint.CUSTOMER_PROFILE_UPDATE,
                ResponseSpecs.requestReturnsOk())
                .put(changeUserNameRequest);

        UserProfileNestedResponse changeUserNameTwiceResponse = new CrudRequester(
                RequestSpecs.authUserSpec(user.getUsername(), user.getPassword()),
                Endpoint.CUSTOMER_PROFILE_GET,
                ResponseSpecs.requestReturnsOk())
                .get()
                .extract()
                .as(UserProfileNestedResponse.class);

        AssertingClass.assertThat(changeUserNameRequest, changeUserNameSecondResponse).match();
        softly.assertThat(changeUserNameTwiceResponse.getName()).isEqualTo(changeUserNameRequest.getName());
        softly.assertThat(changeUserNameSecondResponse.getMessage()).isEqualTo(PROFILE_UPDATED_SUCCESSFULLY.getMessage());
    }


    public static Stream<Arguments> changeNameInvalidCases() {
        return Stream.of(
                Arguments.of("BrittaSmith", NAME_MUST_CONTAINS_TWO_WORDS.getMessage()), // Authorized user cannot change their name without missed space in one world (T30_Negative test)
                Arguments.of("Britta Smith Jons", NAME_MUST_CONTAINS_TWO_WORDS.getMessage()), // Authorized user cannot change their name consist of the three worlds (T30_Negative test)
                Arguments.of("Britta1 Smith", NAME_MUST_CONTAINS_TWO_WORDS.getMessage()), // Authorized user cannot use digits in the name (T31_Negative test)
                Arguments.of("Britta! Smith", NAME_MUST_CONTAINS_TWO_WORDS.getMessage()), // Authorized user cannot use special signs in the name (T31_Negative test)
                Arguments.of("Britta-Maria Smith", NAME_MUST_CONTAINS_TWO_WORDS.getMessage()), // Authorized user cannot use dash in the name (T32_Negative test)
                Arguments.of(" Britta Smith", NAME_MUST_CONTAINS_TWO_WORDS.getMessage()), // Authorized user cannot use space at the beginning of the name (T34_Negative test)
                Arguments.of("Britta Smith ", NAME_MUST_CONTAINS_TWO_WORDS.getMessage()) // Authorized user cannot use space at the end of the name (T35_Negative test)
        );
    }

    @MethodSource("changeNameInvalidCases")
    @ParameterizedTest
    public void authorizedUserCannotChangeNameWithInvalidData(String name, String errorMessage) {
        CreateUserRequest user = AdminStep.createUser();
        UserStep.login(user);

        ChangeUserNameRequest changeUserNameRequest = ChangeUserNameRequest.builder()
                .name(name)
                .build();

        // Authorized third user can change their name on the same name(T33_Positive test)
        new CrudRequester(
                RequestSpecs.authUserSpec(user.getUsername(), user.getPassword()),
                Endpoint.CUSTOMER_PROFILE_UPDATE,
                ResponseSpecs.requestReturnsBadRequest(errorMessage))
                .put(changeUserNameRequest);

        UserProfileNestedResponse changeUserNameResponse = new CrudRequester(
                RequestSpecs.authUserSpec(user.getUsername(), user.getPassword()),
                Endpoint.CUSTOMER_PROFILE_GET,
                ResponseSpecs.requestReturnsOk())
                .get()
                .extract()
                .as(UserProfileNestedResponse.class);

        softly.assertThat(changeUserNameResponse.getName()).isNotEqualTo(name);
        softly.assertThat(changeUserNameResponse.getName()).isNull();
    }
}