package apiTests.iteration2_senior.specs;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FraudStatusNotes {
    THIS_TRANSACTION_DOES_NOT_REQUIRED_FRAUD_CHECKING("This transaction does not require fraud checking.");

    private final String note;
}