package uiTests.iteration2_middle;

import apiTests.iteration2_senior.generators.RandomModelGenerator;
import apiTests.iteration2_senior.models.*;
import apiTests.iteration2_senior.steps.AdminStep;
import apiTests.iteration2_senior.steps.UserStep;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import uiTests.iteration2_middle.models.MatchingTransactionsUI;
import uiTests.iteration2_middle.pages.*;

import java.util.List;

import static apiTests.iteration2_senior.models.TransactionType.DEPOSIT;
import static org.assertj.core.api.Assertions.assertThat;

public class DepositMoneyMiddleUiTest extends BaseUiTest {

    @Test
    public void authorizedUserDepositsMoneySuccessfully() {
        CreateUserRequest user = AdminStep.createUser();
        authAsUser(user);
        CreateUserAccountResponse userAccount = UserStep.createUserAccount(user);
        String accountNumber = userAccount.getAccountNumber();
        String depositMoneyRequest = Double.toString(RandomModelGenerator.generate(DepositMoneyRequest.class)
                .getBalance());

        MatchingTransactionsUI matchingTransactionsUI = new UserDashboard()
                .open()
                .chooseDepositButton()
                .getPage(DepositPage.class)
                .chooseAnAccountOption()
                .chooseAnAccountNumberOption(accountNumber)
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
                .checkMatchingDepositTransactions(depositMoneyRequest);

        assertThat(matchingTransactionsUI.getType()).isEqualTo(DEPOSIT.getType());
        assertThat(matchingTransactionsUI.getAmount()).isEqualTo(depositMoneyRequest);

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
    public void authorizedUserCannotDepositInvalidAmount() {
        CreateUserRequest user = AdminStep.createUser();
        authAsUser(user);
        CreateUserAccountResponse userAccount = UserStep.createUserAccount(user);
        String accountNumber = userAccount.getAccountNumber();
        Faker faker = new Faker();
        double depositAmount = faker.number().randomDouble(2, 5001, 100000);
        String depositMoneyRequest = String.valueOf(depositAmount);

        new UserDashboard()
                .open()
                .chooseDepositButton()
                .getPage(DepositPage.class)
                .chooseAnAccountOption()
                .chooseAnAccountNumberOption(accountNumber)
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

        List<AccountsNestedResponse> existingUserAccounts = new UserStep(user.getUsername(), user.getPassword())
                .getAllAccounts();
        assertThat(existingUserAccounts).isNotEmpty();

        int userDepositResponse = existingUserAccounts.get(0)
                .getTransactions().size();
        assertThat(userDepositResponse).isEqualTo(0);
    }

    @Test
    public void authorizedUserCannotDepositToNonExistingAccount() {
        CreateUserRequest user = AdminStep.createUser();
        authAsUser(user);
        String depositMoneyRequest = Double.toString(RandomModelGenerator.generate(DepositMoneyRequest.class)
                .getBalance());

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

        List<AccountsNestedResponse> existingUserAccounts = new UserStep(user.getUsername(), user.getPassword())
                .getAllAccounts();
        assertThat(existingUserAccounts).isEmpty();
    }
}