package iteration2_middle.utils;

import org.apache.commons.lang3.RandomStringUtils;

public class RandomDataGenerator {
    private RandomDataGenerator(){};

    public static String getUsername() {
        return RandomStringUtils.randomAlphanumeric(7) + ".-_";
    }

    public static String getPassword() {
        return RandomStringUtils.randomNumeric(5) +
                RandomStringUtils.randomAlphabetic(2).toLowerCase() +
                RandomStringUtils.randomAlphabetic(2).toUpperCase() + "$";
    }

    public static String getName() {
        return RandomStringUtils.randomAlphabetic(5) +
                RandomStringUtils.randomAlphabetic(5);
    }
}