package uiTests.iteration2_middle;

import apiTests.iteration2_senior.generators.RandomModelGenerator;
import apiTests.iteration2_senior.models.*;
import apiTests.iteration2_senior.steps.AdminStep;
import apiTests.iteration2_senior.steps.DepositStep;
import apiTests.iteration2_senior.steps.UserStep;
import net.datafaker.Faker;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import uiTests.iteration2_middle.pages.AlertMessages;
import uiTests.iteration2_middle.pages.TransferHistoryPage;
import uiTests.iteration2_middle.pages.TransferPage;
import uiTests.iteration2_middle.pages.UserDashboard;

import java.util.List;

import static apiTests.iteration2_senior.models.TransactionType.TRANSFER_IN;
import static apiTests.iteration2_senior.models.TransactionType.TRANSFER_OUT;
import static apiTests.iteration2_senior.utils.RepeatUtil.repeat;
import static org.assertj.core.api.Assertions.assertThat;

public class TransferMoneyMiddleUiTest extends BaseUiTest {

    @Disabled("learning purpose")
    public void authorizedUserTransfersMoneySuccessfully() {
        CreateUserRequest user = AdminStep.createUser();
        authAsUser(user);
        CreateUserAccountResponse firstUserAccount = UserStep.createUserAccount(user);
        String firstAccountNumber = firstUserAccount.getAccountNumber();
        double deposit = 5000;
        repeat(2, () -> DepositStep.depositMoney(user, firstUserAccount, deposit));
        CreateUserAccountResponse secondUserAccount = UserStep.createUserAccount(user);
        String secondAccountNumber = secondUserAccount.getAccountNumber();
        String transferMoneyRequest = Double.toString(RandomModelGenerator.generate(TransferMoneyRequest.class)
                .getAmount());

        List<String> historyText = new UserDashboard()
                .open()
                .chooseTransferButton()
                .getPage(TransferPage.class)
                .chooseAnAccountOption()
                .chooseAccountNumberOption(firstAccountNumber)
                .enterRecipientName(firstAccountNumber)
                .enterRecipientAccountNumber(secondAccountNumber)
                .enterAmount(transferMoneyRequest)
                .enableCheckBox()
                .clickSendTransferButton()
                .checkAlertMessageAndAccept(
                        AlertMessages.SUCCESSFULLY_TRANSFERRED.getMessage()
                                + transferMoneyRequest
                                + AlertMessages.TO_ACCOUNT.getMessage()
                                + secondAccountNumber
                                + AlertMessages.EXCLAMATION_MARK.getMessage())
                .checkUserStaysOnTransferPage()
                .returnToHomePage()
                .getPage(UserDashboard.class)
                .chooseTransferButton()
                .getPage(TransferPage.class)
                .chooseTransferAgainButton()
                .getPage(TransferHistoryPage.class)
                .getHistoryText();

        assertThat(historyText).anyMatch(t -> t.contains(TRANSFER_IN.getType() + " - $" + transferMoneyRequest));
        assertThat(historyText).anyMatch(t -> t.contains(TRANSFER_OUT.getType() + " - $" + transferMoneyRequest));

        List<AccountsNestedResponse> existingUserAccounts = new UserStep(user.getUsername(), user.getPassword())
                .getAllAccounts();
        assertThat(existingUserAccounts).isNotEmpty();

        List<TransactionNestedResponse> allUserTransactions = existingUserAccounts
                .stream()
                .flatMap(tr -> tr.getTransactions().stream())
                .toList();

        TransactionNestedResponse userTransferInResponse = allUserTransactions
                .stream()
                .filter(t -> TRANSFER_IN.getType().equals(t.getType()))
                .findFirst()
                .orElseThrow();

        TransactionNestedResponse userTransferOutResponse = allUserTransactions
                .stream()
                .filter(t -> TRANSFER_OUT.getType().equals(t.getType()))
                .findFirst()
                .orElseThrow();

        assertThat(userTransferInResponse.getType()).isEqualTo(TRANSFER_IN.getType());
        assertThat(Double.toString(userTransferInResponse.getAmount())).isEqualTo(transferMoneyRequest);
        assertThat(userTransferOutResponse.getType()).isEqualTo(TRANSFER_OUT.getType());
        assertThat(Double.toString(userTransferOutResponse.getAmount())).isEqualTo(transferMoneyRequest);
    }


    @Disabled("learning purpose")
    public void authorizedUserCannotTransferMoneyWithInvalidAmount() {
        CreateUserRequest user = AdminStep.createUser();
        authAsUser(user);
        CreateUserAccountResponse firstUserAccount = UserStep.createUserAccount(user);
        String firstAccountNumber = firstUserAccount.getAccountNumber();
        int deposit = 5000;
        repeat(2, () -> DepositStep.depositMoney(user, firstUserAccount, deposit));
        CreateUserAccountResponse secondUserAccount = UserStep.createUserAccount(user);
        String secondAccountNumber = secondUserAccount.getAccountNumber();
        Faker faker = new Faker();
        double transferAmount = faker.number().randomDouble(2, 10001, 100000);
        String transferMoneyRequest = String.valueOf(transferAmount);

        List<String> historyText = new UserDashboard()
                .open()
                .chooseTransferButton()
                .getPage(TransferPage.class)
                .chooseAnAccountOption()
                .chooseAccountNumberOption(firstAccountNumber)
                .enterRecipientName(firstAccountNumber)
                .enterRecipientAccountNumber(secondAccountNumber)
                .enterAmount(transferMoneyRequest)
                .enableCheckBox()
                .clickSendTransferButton()
                .checkAlertMessageAndAccept(
                        AlertMessages.ERROR_TRANSFER_AMOUNT_CANNOT_EXCEED_10000.getMessage())
                .checkUserStaysOnTransferPage()
                .returnToHomePage()
                .getPage(UserDashboard.class)
                .chooseTransferButton()
                .getPage(TransferPage.class)
                .chooseTransferAgainButton()
                .getPage(TransferHistoryPage.class)
                .getHistoryText();

        assertThat(historyText).noneMatch(t -> t.contains(TRANSFER_IN.getType() + " - $" + transferMoneyRequest));
        assertThat(historyText).noneMatch(t -> t.contains(TRANSFER_OUT.getType() + " - $" + transferMoneyRequest));

        List<AccountsNestedResponse> existingUserAccounts = new UserStep(user.getUsername(), user.getPassword())
                .getAllAccounts();
        assertThat(existingUserAccounts).isNotEmpty();

        List<TransactionNestedResponse> allUserTransactions = existingUserAccounts
                .stream()
                .flatMap(tr -> tr.getTransactions().stream())
                .toList();

        assertThat(allUserTransactions).noneMatch(t -> TRANSFER_IN.getType().equals(t.getType()));
        assertThat(allUserTransactions).noneMatch(t -> TRANSFER_OUT.getType().equals(t.getType()));
    }

    @Disabled("learning purpose")
    public void authorizedUserCannotTransferMoneyWithMissingConfirmation() {
        CreateUserRequest user = AdminStep.createUser();
        authAsUser(user);
        CreateUserAccountResponse firstUserAccount = UserStep.createUserAccount(user);
        String firstAccountNumber = firstUserAccount.getAccountNumber();
        int deposit = 5000;
        repeat(2, () -> DepositStep.depositMoney(user, firstUserAccount, deposit));
        CreateUserAccountResponse secondUserAccount = UserStep.createUserAccount(user);
        String secondAccountNumber = secondUserAccount.getAccountNumber();
        String transferMoneyRequest = Double.toString(RandomModelGenerator.generate(TransferMoneyRequest.class)
                .getAmount());

        List<String> historyText = new UserDashboard()
                .open()
                .chooseTransferButton()
                .getPage(TransferPage.class)
                .chooseAnAccountOption()
                .chooseAccountNumberOption(firstAccountNumber)
                .enterRecipientName(firstAccountNumber)
                .enterRecipientAccountNumber(secondAccountNumber)
                .enterAmount(transferMoneyRequest)
                .disableCheckBox()
                .clickSendTransferButton()
                .checkAlertMessageAndAccept(
                        AlertMessages.PLEASE_FILL_ALL_FIELDS_AND_CONFIRM.getMessage())
                .checkUserStaysOnTransferPage()
                .returnToHomePage()
                .getPage(UserDashboard.class)
                .chooseTransferButton()
                .getPage(TransferPage.class)
                .chooseTransferAgainButton()
                .getPage(TransferHistoryPage.class)
                .getHistoryText();

        assertThat(historyText).noneMatch(t -> t.contains(TRANSFER_IN.getType() + " - $" + transferMoneyRequest));
        assertThat(historyText).noneMatch(t -> t.contains(TRANSFER_OUT.getType() + " - $" + transferMoneyRequest));

        List<AccountsNestedResponse> existingUserAccounts = new UserStep(user.getUsername(), user.getPassword())
                .getAllAccounts();
        assertThat(existingUserAccounts).isNotEmpty();

        List<TransactionNestedResponse> allUserTransactions = existingUserAccounts
                .stream()
                .flatMap(tr -> tr.getTransactions().stream())
                .toList();

        assertThat(allUserTransactions).noneMatch(t -> TRANSFER_IN.getType().equals(t.getType()));
        assertThat(allUserTransactions).noneMatch(t -> TRANSFER_OUT.getType().equals(t.getType()));
    }
}