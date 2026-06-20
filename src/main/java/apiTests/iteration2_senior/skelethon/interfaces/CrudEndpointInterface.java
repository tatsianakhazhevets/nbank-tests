package apiTests.iteration2_senior.skelethon.interfaces;

import apiTests.iteration2_senior.models.BaseModel;

public interface CrudEndpointInterface {
    Object post(BaseModel model);

    Object get(int id);

    Object get();

    Object update(int id, BaseModel model);

    Object put(BaseModel model);

    Object delete(int id);
}