package uiTests.iteration2_senior;

import apiTests.iteration2_senior.generators.RandomModelGenerator;
import apiTests.iteration2_senior.models.*;
import apiTests.iteration2_senior.steps.DepositStep;
import apiTests.iteration2_senior.steps.UserStep;
import common.annotations.APIVersion;
import common_iteration2.annotations.MyUserSession;
import common_iteration2.annotations.UserAccount;
import common_iteration2.storage.AccountStorage;
import common_iteration2.storage.MySessionStorage;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import uiTests.iteration2_senior.elements.TransactionType;
import uiTests.iteration2_senior.pages.AlertMessages;
import uiTests.iteration2_senior.pages.TransferHistoryPage;
import uiTests.iteration2_senior.pages.TransferPage;
import uiTests.iteration2_senior.pages.UserDashboard;

import java.util.List;

import static apiTests.iteration2_senior.models.TransactionType.TRANSFER_IN;
import static apiTests.iteration2_senior.models.TransactionType.TRANSFER_OUT;
import static apiTests.iteration2_senior.utils.RepeatUtil.repeat;
import static org.assertj.core.api.Assertions.assertThat;

@APIVersion("with_validation_fix")
public class TransferMoneySeniorUiTest extends BaseUiSeniorTest {

    @Test
    @MyUserSession
    @UserAccount(amount = 2)
    public void authorizedUserTransfersMoneySuccessfully() {
        //1. Test data
        CreateUserRequest user = MySessionStorage.getUserFromStorage();
        CreateUserAccountResponse firstUserAccount = AccountStorage.getUserAccountNumber(1, 1);
        String firstAccountNumber = AccountStorage.getUserAccountNumber(1, 1).getAccountNumber();
        double deposit = 5000;
        repeat(2, () -> DepositStep.depositMoney(user, firstUserAccount, deposit));
        String secondAccountNumber = AccountStorage.getUserAccountNumber(1, 2).getAccountNumber();
        double amount = RandomModelGenerator.generate(TransferMoneyRequest.class).getAmount();
        String transferMoneyRequest = Double.toString(amount);
        String transferMoneyRequestUiExpected =  String.format("%.2f", amount).replace(",", ".");

        //2. Test steps
        List<TransactionType> transactions = new UserDashboard()
                .open()
                .chooseTransferButton()
                .getPage(TransferPage.class)
                .chooseAccount(firstAccountNumber)
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
                .getAllTransactions();

        //3. Test Results
        assertThat(transactions).anyMatch(t -> t.getType().equals(TRANSFER_IN.getType())
                && t.getAmount().equals(transferMoneyRequestUiExpected));
        assertThat(transactions).anyMatch(t -> t.getType().equals(TRANSFER_OUT.getType())
                && t.getAmount().equals(transferMoneyRequestUiExpected));

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


    @Test
    @MyUserSession
    @UserAccount(amount = 2)
    public void authorizedUserCannotTransferMoneyWithInvalidAmount() {
        //1. Test data
        CreateUserRequest user = MySessionStorage.getUserFromStorage();
        CreateUserAccountResponse firstUserAccount = AccountStorage.getUserAccountNumber(1, 1);
        String firstAccountNumber = AccountStorage.getUserAccountNumber(1, 1).getAccountNumber();
        int deposit = 5000;
        repeat(2, () -> DepositStep.depositMoney(user, firstUserAccount, deposit));
        String secondAccountNumber = AccountStorage.getUserAccountNumber(1, 2).getAccountNumber();
        Faker faker = new Faker();
        double transferAmount = faker.number().randomDouble(2, 10001, 100000);
        String transferMoneyRequest = String.valueOf(transferAmount);

        //2. Test steps
        List<TransactionType> transactions = new UserDashboard()
                .open()
                .chooseTransferButton()
                .getPage(TransferPage.class)
                .chooseAccount(firstAccountNumber)
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
                .getAllTransactions();

        //3. Test Results
        assertThat(transactions).noneMatch(t -> t.getType().equals(TRANSFER_IN.getType())
                && t.getAmount().equals(transferMoneyRequest));
        assertThat(transactions).noneMatch(t -> t.getType().equals(TRANSFER_IN.getType())
                && t.getAmount().equals(transferMoneyRequest));

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


    @Test
    @MyUserSession
    @UserAccount(amount = 2)
    public void authorizedUserCannotTransferMoneyWithMissingConfirmation() {
        //1. Test data
        CreateUserRequest user = MySessionStorage.getUserFromStorage();
        CreateUserAccountResponse firstUserAccount = AccountStorage.getUserAccountNumber(1, 1);
        String firstAccountNumber = AccountStorage.getUserAccountNumber(1, 1).getAccountNumber();
        int deposit = 5000;
        repeat(2, () -> DepositStep.depositMoney(user, firstUserAccount, deposit));
        String secondAccountNumber = AccountStorage.getUserAccountNumber(1, 2).getAccountNumber();
        double amount = RandomModelGenerator.generate(TransferMoneyRequest.class).getAmount();
        String transferMoneyRequest = Double.toString(amount);
        String transferMoneyRequestUiExpected =  String.format("%.2f", amount).replace(",", ".");

        //2. Test steps
        List<TransactionType> transactions = new UserDashboard()
                .open()
                .chooseTransferButton()
                .getPage(TransferPage.class)
                .chooseAccount(firstAccountNumber)
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
                .getAllTransactions();

        //3. Test Results
        assertThat(transactions).noneMatch(t -> t.getType().equals(TRANSFER_IN.getType())
                && t.getAmount().equals(transferMoneyRequestUiExpected));
        assertThat(transactions).noneMatch(t -> t.getType().equals(TRANSFER_IN.getType())
                && t.getAmount().equals(transferMoneyRequestUiExpected));

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