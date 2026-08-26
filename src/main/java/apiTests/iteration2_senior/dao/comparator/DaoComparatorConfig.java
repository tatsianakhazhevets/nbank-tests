package apiTests.iteration2_senior.dao.comparator;

import java.util.Map;
import java.util.Set;

public class DaoComparatorConfig {
    public static final Map<String, Set<String>> MAPPINGS = Map.of(

            "TransactionNestedResponse:TransactionDao", Set.of(
                    "amount=amount",
                    "type=type",
                    "relatedAccountId=relatedAccountId"
            ),

            "UserProfileNestedResponse:CustomerDao", Set.of(
                    "name=name"
            )
    );
}