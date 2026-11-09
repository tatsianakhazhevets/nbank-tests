package apiTests.iteration2_senior.specs;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Descriptions {
    TEST_TRANSFER_WITH_FRAUD_CHECK("Test transfer with fraud check");

    private final String descriptions;
}