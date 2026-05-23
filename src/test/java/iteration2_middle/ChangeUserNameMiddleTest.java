package iteration2_middle;

import iteration2_middle.models.*;
import iteration2_middle.requests.AdminCreateUserRequester;
import iteration2_middle.requests.ChangeUserNameRequester;
import iteration2_middle.requests.LoginUserRequester;
import iteration2_middle.requests.RetrieveUserProfileRequester;
import iteration2_middle.specs.RequestSpecs;
import iteration2_middle.specs.ResponseSpecs;
import iteration2_middle.utils.RandomDataGenerator;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class ChangeUserNameMiddleTest extends BaseTest {

    @Test
    public void authorizedUserCanChangeNameSuccessfully() {

        String name = RandomDataGenerator.getName();

        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomDataGenerator.getUsername())
                .password(RandomDataGenerator.getPassword())
                .role(Role.USER.toString())
                .build();

        new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsCreated())
                .post(createUserRequest);

        new LoginUserRequester(RequestSpecs.unAuthSpec(),
                ResponseSpecs.requestReturnsOk())
                .post(LoginUserRequest.builder()
                        .username(createUserRequest.getUsername())
                        .password(createUserRequest.getPassword())
                        .build())
                .header(RequestSpecs.AUTHORIZATION_HEADER, Matchers.notNullValue());

        ChangeUserNameRequest changeUserNameRequest = ChangeUserNameRequest.builder()
                .name(name)
                .build();

        // Authorized user can change their name (T29_Positive test)
        new ChangeUserNameRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .put(changeUserNameRequest);

        ChangeUserNameResponse changeUserNameResponse = new RetrieveUserProfileRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .get()
                .extract().as(ChangeUserNameResponse.class);

        softly.assertThat(changeUserNameResponse.getName()).isEqualTo(name);

        new ChangeUserNameRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .put(changeUserNameRequest);

        ChangeUserNameResponse changeUserNameTwiceResponse = new RetrieveUserProfileRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .get()
                .extract().as(ChangeUserNameResponse.class);

        softly.assertThat(changeUserNameTwiceResponse.getName()).isEqualTo(name);
    }


    public static Stream<Arguments> changeNameInvalidCases() {
        return Stream.of(
                // Authorized user cannot change their name without missed space in one world (T30_Negative test)
                Arguments.of("BrittaSmith", ResponseSpecs.NAME_MUST_CONTAINS_TWO_WORDS),
                // Authorized user cannot change their name consist of the three worlds (T30_Negative test)
                Arguments.of("Britta Smith Jons", ResponseSpecs.NAME_MUST_CONTAINS_TWO_WORDS),
                // Authorized user cannot use digits in the name (T31_Negative test)
                Arguments.of("Britta1 Smith", ResponseSpecs.NAME_MUST_CONTAINS_TWO_WORDS),
                // Authorized user cannot use special signs in the name (T31_Negative test)
                Arguments.of("Britta! Smith", ResponseSpecs.NAME_MUST_CONTAINS_TWO_WORDS),
                // Authorized user cannot use dash in the name (T32_Negative test)
                Arguments.of("Britta-Maria Smith", ResponseSpecs.NAME_MUST_CONTAINS_TWO_WORDS),
                // Authorized user cannot use space at the beginning of the name (T34_Negative test)
                Arguments.of(" Britta Smith", ResponseSpecs.NAME_MUST_CONTAINS_TWO_WORDS),
                // Authorized user cannot use space at the end of the name (T35_Negative test)
                Arguments.of("Britta Smith ", ResponseSpecs.NAME_MUST_CONTAINS_TWO_WORDS)
        );
    }

    @MethodSource("changeNameInvalidCases")
    @ParameterizedTest
    public void authorizedUserCannotChangeNameWithInvalidData(String name, String errorMessage) {

        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomDataGenerator.getUsername())
                .password(RandomDataGenerator.getPassword())
                .role(Role.USER.toString())
                .build();

        new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsCreated())
                .post(createUserRequest);

        new LoginUserRequester(RequestSpecs.unAuthSpec(),
                ResponseSpecs.requestReturnsOk())
                .post(LoginUserRequest.builder()
                        .username(createUserRequest.getUsername())
                        .password(createUserRequest.getPassword())
                        .build())
                .header(RequestSpecs.AUTHORIZATION_HEADER, Matchers.notNullValue());

        ChangeUserNameRequest changeUserNameRequest = ChangeUserNameRequest.builder()
                .name(name)
                .build();

        new ChangeUserNameRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest(errorMessage))
                .put(changeUserNameRequest);

        ChangeUserNameResponse changeUserNameResponse = new RetrieveUserProfileRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .get()
                .extract().as(ChangeUserNameResponse.class);

        softly.assertThat(changeUserNameResponse.getName()).isNotEqualTo(name);
        softly.assertThat(changeUserNameResponse.getName()).isNull();
    }
}