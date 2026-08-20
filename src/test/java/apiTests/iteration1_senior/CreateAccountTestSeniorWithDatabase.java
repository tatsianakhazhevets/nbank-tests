package apiTests.iteration1_senior;

import apiTests.iteration1_senior.dao.AccountDao;
import apiTests.iteration1_senior.dao.comparison.DaoAndModelAssertions;
import apiTests.iteration1_senior.models.CreateAccountResponse;
import apiTests.iteration1_senior.models.CreateUserRequest;
import apiTests.iteration1_senior.skelethon.Endpoint;
import apiTests.iteration1_senior.skelethon.requesters.ValidatedCrudRequester;
import apiTests.iteration1_senior.specs.RequestSpecs;
import apiTests.iteration1_senior.specs.ResponseSpecs;
import apiTests.iteration1_senior.steps.AdminSteps;
import apiTests.iteration1_senior.steps.DataBaseSteps;
import common.annotations.APIVersion;
import org.junit.jupiter.api.Test;

@APIVersion("with_database_with_fix")
public class CreateAccountTestSeniorWithDatabase extends BaseTestSenior {
    @Test
    public void userCanCreateAccountTest() {

        CreateUserRequest userRequest = AdminSteps.createUser();

        CreateAccountResponse createAccountResponse = new ValidatedCrudRequester<CreateAccountResponse>
                (RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                        Endpoint.ACCOUNTS,
                        ResponseSpecs.entityWasCreated())
                .post(null);

        //Аккаунт был успешно создан в базе данных
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());
        DaoAndModelAssertions.assertThat(createAccountResponse, accountDao).match();
    }
}