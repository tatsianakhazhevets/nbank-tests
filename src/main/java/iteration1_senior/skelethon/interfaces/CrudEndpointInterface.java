package iteration1_senior.skelethon.interfaces;


import iteration1_senior.models.BaseModel;

public interface CrudEndpointInterface {
    Object post(BaseModel model);
    Object get(long id);
    Object update(long id, BaseModel model);
    Object delete(long id);
}