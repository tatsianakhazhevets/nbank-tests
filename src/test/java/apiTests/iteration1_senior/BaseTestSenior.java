package apiTests.iteration1_senior;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class BaseTestSenior {
    protected SoftAssertions softly;

    @BeforeEach
    public void setUpTest(){
        this.softly = new SoftAssertions();
    }

    @AfterEach
    public void afterTest() {
        softly.assertAll();
    }
}