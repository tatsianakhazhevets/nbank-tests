package apiTests.iteration2_senior.models.comparator;

import java.util.Map;
import java.util.Set;

public class ComparatorConfig {

    public static final Map<String, Set<String>> MAPPINGS = Map.of(

            "DepositMoneyRequest:DepositMoneyResponse", Set.of(
                    "id=id",
                    "balance=balance",
                    "balance=transactions[].amount",
                    "id=transactions[].relatedAccountId"
            ),

            "TransferMoneyRequest:TransferMoneyResponse", Set.of(
                    "senderAccountId=senderAccountId",
                    "receiverAccountId=receiverAccountId",
                    "amount=amount"
            ),

            "ChangeUserNameRequest:ChangeUserNameResponse", Set.of(
                    "name=customer.name"
            )
    );
}