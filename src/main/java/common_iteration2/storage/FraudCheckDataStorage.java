package common_iteration2.storage;

import apiTests.iteration2_senior.models.CreateUserAccountResponse;
import apiTests.iteration2_senior.models.CreateUserRequest;
import apiTests.iteration2_senior.models.DepositMoneyFraudCheckVersionResponse;

public class FraudCheckDataStorage {
    private static final ThreadLocal<FraudCheckDataStorage> INSTANCE = ThreadLocal.withInitial(FraudCheckDataStorage::new);

    private CreateUserRequest firstUser;
    private CreateUserAccountResponse firstUserAccount;
    private DepositMoneyFraudCheckVersionResponse depositResponse;
    private double depositAmount;
    private CreateUserRequest secondUser;
    private CreateUserAccountResponse secondUserAccount;

    private FraudCheckDataStorage() {
    }

    public static void saveData(CreateUserRequest firstUser, CreateUserAccountResponse firstUserAccount,
                                DepositMoneyFraudCheckVersionResponse depositResponse,
                                double depositAmount, CreateUserRequest secondUser,
                                CreateUserAccountResponse secondUserAccount) {
        FraudCheckDataStorage storage = INSTANCE.get();
        storage.firstUser = firstUser;
        storage.firstUserAccount = firstUserAccount;
        storage.depositResponse = depositResponse;
        storage.depositAmount = depositAmount;
        storage.secondUser = secondUser;
        storage.secondUserAccount = secondUserAccount;
    }

    public static CreateUserRequest getFirstUser() {
        return INSTANCE.get().firstUser;
    }

    public static CreateUserAccountResponse getFirstUserAccount() {
        return INSTANCE.get().firstUserAccount;
    }

    public static DepositMoneyFraudCheckVersionResponse getDepositResponse() {
        return INSTANCE.get().depositResponse;
    }

    public static double getDepositAmount() {
        return INSTANCE.get().depositAmount;
    }

    public static CreateUserRequest getSecondUser() {
        return INSTANCE.get().secondUser;
    }

    public static CreateUserAccountResponse getSecondUserAccount() {
        return INSTANCE.get().secondUserAccount;
    }

    public static void clearStorage() {
        FraudCheckDataStorage storage = INSTANCE.get();
        storage.firstUser = null;
        storage.firstUserAccount = null;
        storage.depositResponse = null;
        storage.depositAmount = 0;
        storage.secondUser = null;
        storage.secondUserAccount = null;
    }
}