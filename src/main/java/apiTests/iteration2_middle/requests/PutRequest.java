package apiTests.iteration2_middle.requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import apiTests.iteration2_middle.models.BaseModel;

public abstract class PutRequest<T extends BaseModel> {
    protected RequestSpecification requestSpecification;
    protected ResponseSpecification responseSpecification;

    public PutRequest(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        this.requestSpecification = requestSpecification;
        this.responseSpecification = responseSpecification;
    }

    public abstract ValidatableResponse put(T model);
}