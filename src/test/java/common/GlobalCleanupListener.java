package common;

import apiTests.iteration2_senior.BaseTest;
import apiTests.iteration2_senior.models.AdminUsersResponse;
import apiTests.iteration2_senior.skelethon.endpoints.Endpoint;
import apiTests.iteration2_senior.skelethon.requests.ValidatedCrudRequester;
import apiTests.iteration2_senior.specs.RequestSpecs;
import apiTests.iteration2_senior.specs.ResponseSpecs;
import apiTests.iteration2_senior.steps.AdminStep;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;

import java.util.List;

public class GlobalCleanupListener implements LauncherSessionListener {

    @Override
    public void launcherSessionClosed(LauncherSession session) {
        AdminStep.deleteUsers();

        List<AdminUsersResponse> users = new ValidatedCrudRequester<AdminUsersResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USERS_GET,
                ResponseSpecs.requestReturnsOk())
                .getAll(AdminUsersResponse[].class);

        if(!users.isEmpty()) {
            throw new AssertionError("Not all users were deleted: " + users);
        }
    }
}