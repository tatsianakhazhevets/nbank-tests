package common.extensions;

import apiTests.iteration2_senior.models.CreateUserRequest;
import common.annotations.AdminSession;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import uiTests.iteration1_middle.pages.BasePage;

public class AdminSessionExtension implements BeforeEachCallback {
    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        //Шаг 1: есть ли у теста аннотация @AdminSession
        AdminSession annotation = context.getRequiredTestMethod().getAnnotation(AdminSession.class);
        if(annotation != null) { //Шаг 2: Если есть, добавляем в local storage токен админа
            BasePage.authAsUser(CreateUserRequest.getAdmin());
        }
    }
}