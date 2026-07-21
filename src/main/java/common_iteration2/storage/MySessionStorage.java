package common_iteration2.storage;

import apiTests.iteration2_senior.models.CreateUserRequest;
import apiTests.iteration2_senior.steps.UserStep;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class MySessionStorage {
    private static final MySessionStorage INSTANCE = new MySessionStorage();
    private final LinkedHashMap<CreateUserRequest, UserStep> userStorageMap = new LinkedHashMap<>();

    private MySessionStorage(){};

    public static void addUsersInStorage(List<CreateUserRequest> users) {
        for (CreateUserRequest createdUser : users) {
            INSTANCE.userStorageMap.put(createdUser,
                    new UserStep(createdUser.getUsername(), createdUser.getPassword()));
        }
    }

    public static CreateUserRequest getUserFromStorage(int index) {
        return new ArrayList<>(INSTANCE.userStorageMap.keySet()).get(index-1);
    }

    public static CreateUserRequest getUserFromStorage() {
        return getUserFromStorage(1);
    }

    public static UserStep getUserSteps(int index) {
        return new ArrayList<>(INSTANCE.userStorageMap.values()).get(index-1);
    }

    public static UserStep getUserSteps() {
        return getUserSteps(1);
    }

    public static void clearLocalStorage() {
        INSTANCE.userStorageMap.clear();
    }
}