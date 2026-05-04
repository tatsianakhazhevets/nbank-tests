package iteration1;

import iteration1.generators.RandomData;
import iteration1.models.CreateUserRequest;
import iteration1.models.CreateUserResponse;
import iteration1.models.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import iteration1.requests.AdminCreateUserRequester;
import iteration1.specs.RequestSpecs;
import iteration1.specs.ResponseSpecs;

import java.util.List;
import java.util.stream.Stream;

public class CreateUserTestMiddle extends BaseTestMiddle {

    @Test
    public void adminCanCreateUserWithCorrectData() {

        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        CreateUserResponse createUserResponse = new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(createUserRequest)
                .extract().as(CreateUserResponse.class);

        softly.assertThat(createUserRequest.getUsername()).isEqualTo(createUserResponse.getUsername());
        softly.assertThat(createUserRequest.getPassword()).isNotEqualTo(createUserResponse.getPassword());
        softly.assertThat(createUserRequest.getRole()).isEqualTo(createUserResponse.getRole());
    }

    public static Stream<Arguments> userInvalidData() {
        return Stream.of(
                //username field validation
                Arguments.of( "", "Password33$", "USER", "username", List.of("Username cannot be blank",
                        "Username must contain only letters, digits, dashes, underscores, and dots", "Username must be between 3 and 15 characters")),
                Arguments.of( "ab", "Password33$", "USER", "username", List.of("Username must be between 3 and 15 characters")),
                Arguments.of( "abd%", "Password33$", "USER", "username", List.of("Username must contain only letters, digits, dashes, underscores, and dots")),
                Arguments.of( "abd$", "Password33$", "USER", "username", List.of("Username must contain only letters, digits, dashes, underscores, and dots"))
        );
    }

    @MethodSource("userInvalidData")
    @ParameterizedTest
    public void adminCanNotCreateUserWithInvalidData(String username, String password, String role, String errorKey, List<String> errorValue) {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(role)
                .build();

        new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsBadRequest(errorKey, errorValue))
                .post(createUserRequest);
    }

}
