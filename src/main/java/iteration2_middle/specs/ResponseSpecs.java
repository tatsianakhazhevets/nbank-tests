package iteration2_middle.specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;

public class ResponseSpecs {

    public static final String UNAUTHORIZED_ACCESS_TO_ACCOUNT = "Unauthorized access to account";
    public static final String DEPOSIT_AMOUNT_CANNOT_EXCEED_5000 = "Deposit amount cannot exceed 5000";
    public static final String DEPOSIT_AMOUNT_MUST_BE_AT_LEAST_0_01 = "Deposit amount must be at least 0.01";
    public static final String TRANSFER_AMOUNT_CANNOT_EXCEED_10000 = "Transfer amount cannot exceed 10000";
    public static final String TRANSFER_AMOUNT_MUST_BE_AT_LEAST_0_01 = "Transfer amount must be at least 0.01";
    public static final String INVALID_TRANSFER = "Invalid transfer: insufficient funds or invalid accounts";
    public static final String NAME_MUST_CONTAINS_TWO_WORDS = "Name must contain two words with letters only";
    public static final String TRANSFER_SUCCESSFUL = "Transfer successful";
    public static final String PROFILE_UPDATED_SUCCESSFULLY = "Profile updated successfully";


    private ResponseSpecs() {
    }

    private static ResponseSpecBuilder defaultResponseSpec() {
        return new ResponseSpecBuilder();
    }

    public static ResponseSpecification requestReturnsCreated() {
        return defaultResponseSpec()
                .expectStatusCode(HttpStatus.SC_CREATED)
                .build();
    }

    public static ResponseSpecification requestReturnsOk() {
        return defaultResponseSpec()
                .expectStatusCode(HttpStatus.SC_OK)
                .build();
    }

    public static ResponseSpecification requestReturnsBadRequest(String errorMessage) {
        return defaultResponseSpec()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody(Matchers.equalTo(errorMessage))
                .build();
    }

    public static ResponseSpecification requestReturnsForbidden(String errorMessage) {
        return defaultResponseSpec()
                .expectStatusCode(HttpStatus.SC_FORBIDDEN)
                .expectBody((Matchers.equalTo(errorMessage)))
                .build();
    }
}