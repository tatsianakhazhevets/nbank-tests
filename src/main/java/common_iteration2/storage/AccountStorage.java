package common_iteration2.storage;

import apiTests.iteration2_senior.models.CreateUserAccountResponse;
import apiTests.iteration2_senior.models.CreateUserRequest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class AccountStorage {
    private static final AccountStorage INSTANCE = new AccountStorage();
    private final LinkedHashMap<CreateUserRequest, List<CreateUserAccountResponse>> accountStorageMap = new LinkedHashMap<>();

    private AccountStorage() {};

    public static void addUserAccountNumber(CreateUserRequest user, CreateUserAccountResponse accountNumber) {
        if (!INSTANCE.accountStorageMap.containsKey(user)) {
            INSTANCE.accountStorageMap.put(user, new ArrayList<>());
        }
        INSTANCE.accountStorageMap.get(user).add(accountNumber);
    }

    public static void addUserAccountNumbers(CreateUserRequest user, List<CreateUserAccountResponse> accountNumber) {
        if (!INSTANCE.accountStorageMap.containsKey(user)) {
            INSTANCE.accountStorageMap.put(user, new ArrayList<>());
        }
        INSTANCE.accountStorageMap.get(user).addAll(accountNumber);
    }

    public static CreateUserAccountResponse getUserAccountNumber(int userIndex, int accountIndex) {
        CreateUserRequest user = MySessionStorage.getUserFromStorage(userIndex);
        return INSTANCE.accountStorageMap.get(user).get(accountIndex - 1);
    }

    public static CreateUserAccountResponse getUserAccountNumber(int userIndex) {
        return getUserAccountNumber(userIndex, 1);
    }

    public static CreateUserAccountResponse getUserAccountNumber() {
        return getUserAccountNumber(1, 1);
    }

    public static List<CreateUserAccountResponse> getAccounts(int userIndex) {
        CreateUserRequest user = MySessionStorage.getUserFromStorage(userIndex);
        return INSTANCE.accountStorageMap.get(user);
    }

    public static void clearAccountStorage(){
        INSTANCE.accountStorageMap.clear();
    }
}