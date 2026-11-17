package apiTests.iteration2_senior;

import common.extensions.TimingExtension;
import common_iteration2.extensions.FraudCheckDataExtension;
import common_iteration2.extensions.MyFraudCheckMockExtension;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({TimingExtension.class,
        MyFraudCheckMockExtension.class,
        FraudCheckDataExtension.class})
public class BaseTest {
    protected SoftAssertions softly;

    @BeforeEach
    public void setUpTest() {
        this.softly = new SoftAssertions();
    }

    @AfterEach
    public void afterTest() {
        softly.assertAll();
    }
}