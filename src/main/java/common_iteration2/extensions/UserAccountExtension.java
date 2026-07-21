package common_iteration2.extensions;

import apiTests.iteration2_senior.models.CreateUserAccountResponse;
import apiTests.iteration2_senior.models.CreateUserRequest;
import apiTests.iteration2_senior.steps.UserStep;
import common_iteration2.annotations.UserAccount;
import common_iteration2.storage.AccountStorage;
import common_iteration2.storage.MySessionStorage;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class UserAccountExtension implements BeforeEachCallback {
    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        UserAccount annotation = context.getRequiredTestMethod().getAnnotation(UserAccount.class);

        if(annotation != null) {
            AccountStorage.clearAccountStorage();
            int createdAccount = annotation.amount();
            int userIndex = annotation.user();

            CreateUserRequest user = (userIndex == 0) ? MySessionStorage.getUserFromStorage() : MySessionStorage.getUserFromStorage(userIndex);

            for(int i = 0; i < createdAccount; i++) {
                CreateUserAccountResponse userAccount = UserStep.createUserAccount(user);
                AccountStorage.addUserAccountNumber(user, userAccount);
            }
        }
    }
}