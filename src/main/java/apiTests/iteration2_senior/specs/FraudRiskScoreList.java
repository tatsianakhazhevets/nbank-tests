package apiTests.iteration2_senior.specs;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FraudRiskScoreList {
    TWO_TENTHS(0.2),
    FIFTH_TENTHS(0.5),
    SIX_TENTHS(0.6),
    NINE_TENTHS(0.9),
    ONE(1.0);

    private final double score;
}