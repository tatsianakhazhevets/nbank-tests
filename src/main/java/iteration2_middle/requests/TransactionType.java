package iteration2_middle.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionType {
    DEPOSIT("DEPOSIT"),
    TRANSFER_IN("TRANSFER_IN"),
    TRANSFER_OUT("TRANSFER_OUT");

    private final String type;
}