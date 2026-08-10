package apiTests.iteration2_senior;

import apiTests.iteration2_senior.models.AdminUsersResponse;
import apiTests.iteration2_senior.skelethon.endpoints.Endpoint;
import apiTests.iteration2_senior.skelethon.requests.ValidatedCrudRequester;
import apiTests.iteration2_senior.specs.RequestSpecs;
import apiTests.iteration2_senior.specs.ResponseSpecs;
import apiTests.iteration2_senior.steps.AdminStep;
import common.extensions.TimingExtension;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

@ExtendWith(TimingExtension.class)
public class BaseTest {
    protected SoftAssertions softly;

    @BeforeEach
    public void setUpTest() {
        this.softly = new SoftAssertions();
    }

    @AfterEach
    public void afterTest() {
        softly.assertAll();
    }

    @AfterEach
    public void cleanUp() {
        softly.assertAll();
        AdminStep.deleteUsers();

        List<AdminUsersResponse> users = new ValidatedCrudRequester<AdminUsersResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USERS_GET,
                ResponseSpecs.requestReturnsOk())
                .getAll(AdminUsersResponse[].class);

        softly.assertThat(users).isEmpty();
    }
}