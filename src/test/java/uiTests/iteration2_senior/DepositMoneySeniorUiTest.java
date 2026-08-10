package uiTests.iteration2_senior;

import apiTests.iteration2_senior.generators.RandomModelGenerator;
import apiTests.iteration2_senior.models.*;
import apiTests.iteration2_senior.steps.UserStep;
import common_iteration2.annotations.UserAccount;
import common_iteration2.annotations.MyUserSession;
import common_iteration2.storage.AccountStorage;
import common_iteration2.storage.MySessionStorage;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import uiTests.iteration2_senior.elements.TransactionType;
import uiTests.iteration2_senior.pages.*;

import java.util.List;

import static apiTests.iteration2_senior.models.TransactionType.DEPOSIT;
import static org.assertj.core.api.Assertions.assertThat;

public class DepositMoneySeniorUiTest extends BaseUiSeniorTest{

    @Test
    @MyUserSession
    @UserAccount
    public void authorizedUserDepositsMoneySuccessfully() {
        //1. Test data
        CreateUserRequest user = MySessionStorage.getUserFromStorage();
        String accountNumber = AccountStorage.getUserAccountNumber().getAccountNumber();
        String depositMoneyRequest = Double.toString(RandomModelGenerator.generate(DepositMoneyRequest.class)
                .getBalance());

        //2. Test steps
        TransactionType matchingTransactions = new UserDashboard()
                .open()
                .chooseDepositButton()
                .getPage(DepositPage.class)
                .chooseAccount(accountNumber)
                .enterAmountAndClickDepositButton(depositMoneyRequest)
                .checkAlertMessageAndAccept(AlertMessages.SUCCESSFULLY_DEPOSITED.getMessage()
                        + depositMoneyRequest
                        + AlertMessages.TO_ACCOUNT.getMessage()
                        + accountNumber
                        + AlertMessages.EXCLAMATION_MARK.getMessage())
                .getPage(UserDashboard.class)
                .redirectToUserDashboard()
                .chooseTransferButton()
                .getPage(TransferPage.class)
                .chooseTransferAgainButton()
                .getPage(TransferHistoryPage.class)
                .findTransaction(depositMoneyRequest);

        //3. Test Results
        assertThat(matchingTransactions.getType()).isEqualTo(DEPOSIT.getType());
        assertThat(matchingTransactions.getAmount()).isEqualTo(depositMoneyRequest);

        List<AccountsNestedResponse> existingUserAccounts = new UserStep(user.getUsername(), user.getPassword())
                .getAllAccounts();
        assertThat(existingUserAccounts).isNotEmpty();

        TransactionNestedResponse userDepositResponse = existingUserAccounts.get(0)
                .getTransactions()
                .stream()
                .filter(t -> DEPOSIT.getType().equals(t.getType()))
                .findFirst()
                .orElseThrow();
        assertThat(Double.toString(userDepositResponse.getAmount())).isEqualTo(depositMoneyRequest);
        assertThat(userDepositResponse.getType()).isEqualTo(DEPOSIT.getType());
    }


    @Test
    @MyUserSession
    @UserAccount
    public void authorizedUserCannotDepositInvalidAmount() {
        //1. Test data
        CreateUserRequest user = MySessionStorage.getUserFromStorage();
        String accountNumber = AccountStorage.getUserAccountNumber().getAccountNumber();
        Faker faker = new Faker();
        double depositAmount = faker.number().randomDouble(2, 5001, 100000);
        String depositMoneyRequest = String.valueOf(depositAmount);

        //2. Test steps
        new UserDashboard()
                .open()
                .chooseDepositButton()
                .getPage(DepositPage.class)
                .chooseAccount(accountNumber)
                .enterAmountAndClickDepositButton(depositMoneyRequest)
                .checkAlertMessageAndAccept(AlertMessages.PLEASE_DEPOSIT_LESS_OR_EQUALS_TO_5000$.getMessage())
                .checkUserStaysOnDepositPage()
                .returnToHomePage()
                .getPage(UserDashboard.class)
                .chooseTransferButton()
                .getPage(TransferPage.class)
                .chooseTransferAgainButton()
                .getPage(TransferHistoryPage.class)
                .checkEmptyHistoryTransactions();

        //3. Test Results
        List<AccountsNestedResponse> existingUserAccounts = new UserStep(user.getUsername(), user.getPassword())
                .getAllAccounts();
        assertThat(existingUserAccounts).isNotEmpty();

        int userDepositResponse = existingUserAccounts.get(0)
                .getTransactions().size();
        assertThat(userDepositResponse).isEqualTo(0);
    }

    @Test
    @MyUserSession
    public void authorizedUserCannotDepositToNonExistingAccount() {
        //1. Test data
        CreateUserRequest user = MySessionStorage.getUserFromStorage();
        String depositMoneyRequest = Double.toString(RandomModelGenerator.generate(DepositMoneyRequest.class)
                .getBalance());

        //2. Test steps
        new UserDashboard()
                .open()
                .chooseDepositButton()
                .getPage(DepositPage.class)
                .chooseAnAccountOptionWithoutCreatedAccount()
                .enterAmountAndClickDepositButton(depositMoneyRequest)
                .checkAlertMessageAndAccept(AlertMessages.PLEASE_SELECT_AN_ACCOUNT.getMessage())
                .checkUserStaysOnDepositPage()
                .returnToHomePage()
                .getPage(UserDashboard.class)
                .chooseTransferButton()
                .getPage(TransferPage.class)
                .chooseTransferAgainButton()
                .getPage(TransferHistoryPage.class)
                .checkEmptyHistoryTransactions();

        //3. Test Results
        List<AccountsNestedResponse> existingUserAccounts = new UserStep(user.getUsername(), user.getPassword())
                .getAllAccounts();
        assertThat(existingUserAccounts).isEmpty();
    }
}