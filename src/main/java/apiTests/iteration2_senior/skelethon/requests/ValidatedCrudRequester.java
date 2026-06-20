package apiTests.iteration2_senior.skelethon.requests;

import io.restassured.common.mapper.TypeRef;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import apiTests.iteration2_senior.models.BaseModel;
import apiTests.iteration2_senior.skelethon.endpoints.Endpoint;
import apiTests.iteration2_senior.skelethon.interfaces.CrudEndpointInterface;
import apiTests.iteration2_senior.skelethon.interfaces.GetAllEndpointInterface;
import apiTests.iteration2_senior.skelethon.settings.HttpRequest;

import java.util.List;

public class ValidatedCrudRequester<T extends BaseModel> extends HttpRequest implements CrudEndpointInterface, GetAllEndpointInterface {

    private CrudRequester crudRequester;

    public ValidatedCrudRequester(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
        this.crudRequester = new CrudRequester(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    public T post(BaseModel model) {
        return (T) crudRequester.post(model).extract().as(endpoint.getResponseModel());
    }

    @Override
    public T get(int id) {
        return (T) crudRequester.get(id).extract().as(endpoint.getResponseModel());
    }

    @Override
    public T get() {
        return (T) crudRequester.get().extract().as(endpoint.getResponseModel());
    }

    @Override
    public T update(int id, BaseModel model) {
        return (T) crudRequester.update(id, model).extract().as(endpoint.getResponseModel());
    }

    @Override
    public T put(BaseModel model) {
        return (T) crudRequester.put(model).extract().as(endpoint.getResponseModel());
    }

    @Override
    public ValidatableResponse delete(int id) {
        return (ValidatableResponse) crudRequester.delete(id);
    }

    @Override
    public ValidatableResponse getAll() {
        return crudRequester.getAll();
    }

    public List<T> getAll(TypeRef<List<T>> typeRef) {
        return getAll().extract().as(typeRef);
    }
}