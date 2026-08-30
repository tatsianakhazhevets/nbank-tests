package apiTests.iteration2_senior.steps;

import apiTests.iteration2_senior.dao.AccountDao;
import apiTests.iteration2_senior.dao.CustomerDao;
import apiTests.iteration2_senior.dao.TransactionDao;
import apiTests.iteration2_senior.dao.sql.AccountSQL;
import apiTests.iteration2_senior.dao.sql.TransactionSQL;
import apiTests.iteration2_senior.dao.sql.UserProfileSQL;

import java.util.List;

public class DataBaseStep {

    public static AccountDao getAccountByAccountNumber(String accountNumber) {
        AccountSQL accountSQL = new AccountSQL();
        return accountSQL.getByAccountNumber(accountNumber);
    }

    public static TransactionDao getTransactionByAccountId(Integer accountId) {
        TransactionSQL transactionSQL = new TransactionSQL();
        return transactionSQL.getTransactionByAccountId(accountId);
    }

    public static List<TransactionDao> getTransactionsByAccountId(Integer accountId) {
        TransactionSQL transactionSQL = new TransactionSQL();
        return transactionSQL.getTransactionsByAccountId(accountId);
    }

    public static CustomerDao getUserProfileByUsername(String username) {
        UserProfileSQL userProfileSQL = new UserProfileSQL();
        return userProfileSQL.getProfileByUsername(username);
    }
}