package apiTests.iteration1_middle;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class BaseTestMiddle {
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
