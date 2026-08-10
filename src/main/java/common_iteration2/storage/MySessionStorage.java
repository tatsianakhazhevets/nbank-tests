package common_iteration2.storage;

import apiTests.iteration2_senior.models.CreateUserRequest;
import apiTests.iteration2_senior.steps.UserStep;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class MySessionStorage {
    /* ThreadLocal (для общих сущностей) - способ сделать SessionStorage потокобезопасным
    Каждый поток обращаясь к INSTANCE.get() получает свою КОПИЮ!
    Под капотом хранится такая мапа:
    Map<Thread, SessionStorage> - обращаясь мы получаем ключ-его поток и соответствующую сессию
    Test 1 -> создал юзеров, положил в SessionStorage(с ThreadLocal тест работает со своей копией1), работают с ними
    Test 2 -> создал юзеров, положил в SessionStorage(с ThreadLocal тест работает со своей копией2), работают с ними
    Test 3 -> создал юзеров, положил в SessionStorage(с ThreadLocal тест работает со своей копией3), работают с ними
    то есть, с ThreadLocal они не будут влиять друг на друга! таким образом мы обезопашиваем каждый тест и атомарность его исполнения
     */
    private static final ThreadLocal<MySessionStorage> INSTANCE = ThreadLocal.withInitial(MySessionStorage::new);
    private final LinkedHashMap<CreateUserRequest, UserStep> userStorageMap = new LinkedHashMap<>();

    private MySessionStorage(){};

    public static void addUsersInStorage(List<CreateUserRequest> users) {
        for (CreateUserRequest createdUser : users) {
            INSTANCE.get().userStorageMap.put(createdUser,
                    new UserStep(createdUser.getUsername(), createdUser.getPassword()));
        }
    }

    public static CreateUserRequest getUserFromStorage(int index) {
        return new ArrayList<>(INSTANCE.get().userStorageMap.keySet()).get(index-1);
    }

    public static CreateUserRequest getUserFromStorage() {
        return getUserFromStorage(1);
    }

    public static UserStep getUserSteps(int index) {
        return new ArrayList<>(INSTANCE.get().userStorageMap.values()).get(index-1);
    }

    public static UserStep getUserSteps() {
        return getUserSteps(1);
    }

    public static void clearLocalStorage() {
        INSTANCE.get().userStorageMap.clear();
    }
}