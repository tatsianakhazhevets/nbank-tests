package iteration2_middle.utils;

import org.apache.commons.lang3.RandomStringUtils;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class RandomDataGenerator {
    private RandomDataGenerator() {
    }

    ;

    public static String getUsername() {
        return RandomStringUtils.randomAlphanumeric(7) + ".-_";
    }

    public static String getPassword() {
        return RandomStringUtils.randomNumeric(5) +
                RandomStringUtils.randomAlphabetic(2).toLowerCase() +
                RandomStringUtils.randomAlphabetic(2).toUpperCase() + "$";
    }

    public static String getName() {
        return RandomStringUtils.randomAlphabetic(5) + " " +
                RandomStringUtils.randomAlphabetic(5);
    }

    public static double getDeposit() {
        double minDeposit = 0.01;
        double maxDeposit = 5000.00;
        double deposit = minDeposit + (Math.random() * (maxDeposit - minDeposit));
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat df = new DecimalFormat("#.##", symbols);
        df.setRoundingMode(RoundingMode.HALF_UP);
        return Double.parseDouble(df.format(deposit));
    }
}