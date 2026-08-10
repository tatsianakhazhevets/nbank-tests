package common.storage;

import apiTests.iteration1_senior.models.CreateUserRequest;
import apiTests.iteration1_senior.steps.UserSteps;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SessionStorage {
    private static final ThreadLocal<SessionStorage> INSTANCE = ThreadLocal.withInitial(SessionStorage::new);

    private final LinkedHashMap<CreateUserRequest, UserSteps> userStepMap = new LinkedHashMap<>();

    private SessionStorage() {};

    public static void addUsers(List<CreateUserRequest> users) {
        for(CreateUserRequest user: users) {
            INSTANCE.get().userStepMap.put(user, new UserSteps(user.getUsername(), user.getPassword()));
        }
    }

    /**
     * Возвращаем объект CreateUserRequest по его порядковому номеру в списке созданных пользователей.
     * @param index Порядковый номер, начиная с 1 (а не с 0)
     * @return Объект CreateUserRequest, соответствующий указанному порядковому номеру
     */

    public static CreateUserRequest getUser(int index) {
        return new ArrayList<>(INSTANCE.get().userStepMap.keySet()).get(index-1);
    }

    public static CreateUserRequest getUser() {
        return getUser(1);
    }

    public static UserSteps getSteps(int index) {
        return new ArrayList<>(INSTANCE.get().userStepMap.values()).get(index-1);
    }

    public static UserSteps getSteps() {
        return getSteps(1);
    }

    public static void clear() {
        INSTANCE.get().userStepMap.clear();
    }
}