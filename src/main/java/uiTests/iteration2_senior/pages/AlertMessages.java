package uiTests.iteration2_senior.pages;

import lombok.Getter;

@Getter
public enum AlertMessages {
    SUCCESSFULLY_DEPOSITED("✅ Successfully deposited $"),
    TO_ACCOUNT(" to account "),
    EXCLAMATION_MARK("!"),
    PLEASE_DEPOSIT_LESS_OR_EQUALS_TO_5000$("❌ Please deposit less or equal to 5000$."),
    PLEASE_SELECT_AN_ACCOUNT("❌ Please select an account."),
    SUCCESSFULLY_TRANSFERRED("✅ Successfully transferred $"),
    ERROR_TRANSFER_AMOUNT_CANNOT_EXCEED_10000("❌ Error: Transfer amount cannot exceed 10000"),
    PLEASE_FILL_ALL_FIELDS_AND_CONFIRM("❌ Please fill all fields and confirm."),
    NAME_UPDATED_SUCCESSFULLY("✅ Name updated successfully!"),
    NAME_MUST_CONTAIN_TWO_WORDS_WITH_LETTERS_ONLY("Name must contain two words with letters only");

    private final String message;

    AlertMessages(String message) {
        this.message = message;
    }
}