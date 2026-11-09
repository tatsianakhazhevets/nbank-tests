package common_iteration2.extensions;

import apiTests.iteration2_senior.models.CreateUserAccountResponse;
import apiTests.iteration2_senior.models.CreateUserRequest;
import apiTests.iteration2_senior.models.DepositMoneyFraudCheckVersionResponse;
import apiTests.iteration2_senior.steps.AdminStep;
import apiTests.iteration2_senior.steps.DepositStep;
import apiTests.iteration2_senior.steps.UserStep;
import common_iteration2.annotations.FraudCheckData;
import common_iteration2.storage.FraudCheckDataStorage;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class FraudCheckDataExtension implements BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        FraudCheckData annotation = context.getRequiredTestMethod().getAnnotation(FraudCheckData.class);

        if (annotation != null) {
            FraudCheckDataStorage.clearStorage();

            CreateUserRequest firstUser = AdminStep.createUser();
            CreateUserAccountResponse firstUserAccountId = UserStep.createUserAccount(firstUser);
            double deposit = Math.random() * 4999.9 + 0.1;
            DepositMoneyFraudCheckVersionResponse depositMoneyOnFirstUserAccountResponse = DepositStep.depositMoneyWithFraudCheckVersion
                    (firstUser, firstUserAccountId, deposit);
            CreateUserRequest secondUser = AdminStep.createUser();
            CreateUserAccountResponse secondUserAccountId = UserStep.createUserAccount(secondUser);

            FraudCheckDataStorage.saveData(
                    firstUser,
                    firstUserAccountId,
                    depositMoneyOnFirstUserAccountResponse,
                    deposit,
                    secondUser,
                    secondUserAccountId
            );
        }
    }
}