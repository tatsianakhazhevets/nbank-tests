package common_iteration2.extensions;

import apiTests.iteration2_senior.models.CreateUserRequest;
import apiTests.iteration2_senior.steps.AdminStep;
import common_iteration2.annotations.MyUserSession;
import common_iteration2.storage.MySessionStorage;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import uiTests.iteration2_middle.pages.BasePage;

import java.util.LinkedList;
import java.util.List;

public class MyUserSessionExtension implements BeforeEachCallback {
    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        MyUserSession annotation = context.getRequiredTestMethod().getAnnotation(MyUserSession.class);

        if(annotation != null) {
            MySessionStorage.clearLocalStorage();

            int usersCreatedQuantity = annotation.value();
            List<CreateUserRequest> users = new LinkedList<>();

            for(int i = 0; i < usersCreatedQuantity; i++) {
                CreateUserRequest user = AdminStep.createUser();
                users.add(user);
            }

            MySessionStorage.addUsersInStorage(users);
            int authAsUser = annotation.auth();
            BasePage.authAsUser(MySessionStorage.getUserFromStorage(authAsUser));
        }
    }
}