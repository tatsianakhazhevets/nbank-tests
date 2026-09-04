package apiTests.iteration1_senior;

import apiTests.iteration1_senior.models.CreateAccountResponse;
import apiTests.iteration1_senior.models.CreateUserRequest;
import apiTests.iteration1_senior.models.DepositResponse;
import apiTests.iteration1_senior.models.TransferResponse;
import apiTests.iteration1_senior.models.comparison.ModelAssertions;
import apiTests.iteration1_senior.steps.AccountSteps;
import apiTests.iteration1_senior.steps.AdminSteps;
import common.annotations.APIVersion;
import common.annotations.FraudCheckMock;
import common.extensions.FraudCheckWireMockExtension;
import common.extensions.TimingExtension;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@APIVersion("with_fraud_check_with_approve")
@ExtendWith({TimingExtension.class, FraudCheckWireMockExtension.class})
public class TransferWithFraudCheckTest extends BaseTestSenior {
    private CreateUserRequest user1;
    private CreateUserRequest user2;
    private CreateAccountResponse account1;
    private CreateAccountResponse account2;
    private DepositResponse depositResponse;
    private TransferResponse transferResponse;

    @BeforeEach
    public void setupTest() {
        this.softly = new SoftAssertions();
    }

    @Test
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "APPROVED",
            riskScore = 0.2,
            reason = "Low risk transaction",
            requiresManualReview = false,
            additionalVerificationRequired = false
    )
    public void testTransferWithFraudCheck() {
        user1 = AdminSteps.createUser();

        AccountSteps accountSteps1 = new AccountSteps(user1.getUsername(), user1.getPassword());
        account1 = accountSteps1.createAccount();

        double depositAmount = Math.random() * 4999.9 + 0.1;
        depositResponse = accountSteps1.depositToAccount(account1.getId(), depositAmount);

        user2 = AdminSteps.createUser();
        AccountSteps accountSteps2 = new AccountSteps(user2.getUsername(), user2.getPassword());
        account2 = accountSteps2.createAccount();

        double transferAmount = Math.random() * (depositAmount - 0.1) + 0.1;

        //Шаги теста - попытка перевода с проверкой на фрод
        transferResponse = accountSteps1.transferWithFraudCheck(  //смотреть на мок, а не настоящий сервис
                account1.getId(),
                account2.getId(),
                transferAmount
        );

        softly.assertThat(transferResponse).isNotNull();


        TransferResponse expectedResponse = TransferResponse.builder()
                .status("APPROVED")
                .message("Transfer approved and processed immediately")
                .amount(transferAmount)
                .senderAccountId(account1.getId())
                .receiverAccountId(account2.getId())
                .fraudRiskScore(0.2)
                .fraudReason("Low risk transaction")
                .requiresManualReview(false)
                .requiresVerification(false)
                .build();

        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();
    }

    @AfterEach
    public void afterTest() {
        softly.assertAll();
    }
}