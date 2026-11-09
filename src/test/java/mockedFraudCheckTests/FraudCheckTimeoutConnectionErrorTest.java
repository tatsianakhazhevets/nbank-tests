package mockedFraudCheckTests;

import apiTests.iteration2_senior.BaseTest;
import apiTests.iteration2_senior.assertions.AssertingClass;
import apiTests.iteration2_senior.models.FraudCheckStatusResponse;
import apiTests.iteration2_senior.models.TransferWithFraudCheckRequest;
import apiTests.iteration2_senior.models.TransferWithFraudCheckResponse;
import apiTests.iteration2_senior.skelethon.endpoints.Endpoint;
import apiTests.iteration2_senior.skelethon.requests.ValidatedCrudRequester;
import apiTests.iteration2_senior.specs.RequestSpecs;
import apiTests.iteration2_senior.specs.ResponseSpecs;
import common.annotations.APIVersion;
import common_iteration2.annotations.FraudCheckData;
import common_iteration2.annotations.MyFraudCheckMock;
import common_iteration2.storage.FraudCheckDataStorage;
import org.junit.jupiter.api.Test;

import static apiTests.iteration2_senior.specs.Descriptions.*;
import static apiTests.iteration2_senior.specs.FraudMessages.*;
import static apiTests.iteration2_senior.specs.FraudRiskScoreList.*;
import static apiTests.iteration2_senior.specs.FraudStatus.*;
import static apiTests.iteration2_senior.specs.FraudStatusNotes.*;

@APIVersion("with_fraud_check_with_approve")
public class FraudCheckTimeoutConnectionErrorTest extends BaseTest {

    @Test
    @FraudCheckData
    @MyFraudCheckMock(
            status = "SUCCESS",
            decision = "REVIEW_REQUIRED",
            riskScore = 0.5,
            reason = "High risk transaction",
            requiresManualReview = true,
            additionalVerificationRequired = false,
            testType = "negative"
    )
    public void approvedScenario() {
        // 1.Test data
        double depositAmount = FraudCheckDataStorage.getDepositAmount();
        double transferAmount = Math.random() * (depositAmount - 0.1) + 0.1;

        // 2. Test steps
        TransferWithFraudCheckRequest transferWithFraudCheckRequest = TransferWithFraudCheckRequest.builder()
                .senderAccountId(FraudCheckDataStorage.getFirstUserAccount().getId())
                .receiverAccountId(FraudCheckDataStorage.getSecondUserAccount().getId())
                .amount(transferAmount)
                .description(TEST_TRANSFER_WITH_FRAUD_CHECK.getDescriptions())
                .build();

        TransferWithFraudCheckResponse transferWithFraudCheckResponse = new ValidatedCrudRequester<TransferWithFraudCheckResponse>(
                RequestSpecs.authUserSpec(
                        FraudCheckDataStorage.getFirstUser().getUsername(),
                        FraudCheckDataStorage.getFirstUser().getPassword()),
                Endpoint.FRAUD_CHECK_TRANSFER,
                ResponseSpecs.requestReturnsOk())
                .post(transferWithFraudCheckRequest);

        //3. Test Results
        softly.assertThat(transferWithFraudCheckResponse).isNotNull();
        AssertingClass.assertThat(transferWithFraudCheckRequest, transferWithFraudCheckResponse).match();

        TransferWithFraudCheckResponse transferWithFraudCheckExpectedResponse = TransferWithFraudCheckResponse.builder()
                .fraudRiskScore(FIFTH_TENTHS.getScore())
                .receiverAccountId(FraudCheckDataStorage.getSecondUserAccount().getId())
                .requiresVerification(false)
                .message(TRANSFER_REQUIRED_MANUAL_REVIEW.getMessage())
                .amount(transferAmount)
                .status(MANUAL_REVIEW_REQUIRED.getStatus())
                .senderAccountId(FraudCheckDataStorage.getFirstUserAccount().getId())
                .fraudReason(UNEXPECTED_ERROR_DURING_FRAUD_CHECK.getMessage())
                .requiresManualReview(true)
                .build();

        AssertingClass.assertThat(transferWithFraudCheckExpectedResponse, transferWithFraudCheckResponse).match();

        int transactionId = transferWithFraudCheckResponse.getTransactionId();

        FraudCheckStatusResponse fraudCheckStatusResponse = new ValidatedCrudRequester<FraudCheckStatusResponse>(
                RequestSpecs.authUserSpec(
                        FraudCheckDataStorage.getFirstUser().getUsername(),
                        FraudCheckDataStorage.getFirstUser().getPassword()),
                Endpoint.FRAUD_CHECK_TRANSFER_STATUS,
                ResponseSpecs.requestReturnsOk())
                .get(transactionId);

        softly.assertThat(fraudCheckStatusResponse.getStatus())
                .isEqualTo(NO_FRAUD_CHECK_REQUIRED.getStatus());
        softly.assertThat(fraudCheckStatusResponse.getNote())
                .isEqualTo(THIS_TRANSACTION_DOES_NOT_REQUIRED_FRAUD_CHECKING.getNote());
    }
}