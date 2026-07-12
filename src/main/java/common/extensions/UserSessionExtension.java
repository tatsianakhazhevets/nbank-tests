package common.extensions;

import apiTests.iteration1_senior.models.CreateUserRequest;
import apiTests.iteration1_senior.steps.AdminSteps;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import uiTests.iteration1_middle.pages.BasePage;

import java.util.LinkedList;
import java.util.List;

public class UserSessionExtension implements BeforeEachCallback {
    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        //Шаг 1: проверка, что у теста есть аннотация, что у Юзера есть юзер сессион
        UserSession annotation = context.getRequiredTestMethod().getAnnotation(UserSession.class);
        if (annotation != null) {
            int userCount = annotation.value();

            SessionStorage.clear();

            List<CreateUserRequest> users = new LinkedList<>();

            for(int i = 0; i < userCount; i++) {
                CreateUserRequest user = AdminSteps.createUser();
                users.add(user);
            }

            SessionStorage.addUsers(users);
            int authAsUser = annotation.auth();
            BasePage.authAsUser(SessionStorage.getUser(authAsUser));
        }
    }
}