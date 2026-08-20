package apiTests.iteration1_senior.configs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Config INSTANCE = new Config();
    private final Properties properties = new Properties();

    private Config() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("config.properties not found in resources");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Fail to load config.properties");
        }
    }

    /*
    Ищет значение настройки по имени key и проверяет 3 места — от самого приоритетного к менее приоритетному.
    !Если настройку не нашли ни в системных свойствах, ни в переменных окружения — возьми её из .properties файла!
     */
    public static String getProperty(String key) {
        // ПРИОРИТЕТ 1 - это системное свойство baseApiUrl =..
        // key = "baseApiUrl" -> А есть ли у меня системное свойство с названием baseApiUrl?
        String systemValue = System.getProperty(key);

        if (systemValue != null) {
            return systemValue;
        }

        // ПРИОРИТЕТ 2 - это переменная окружения baseApiUrl - BASEAPIURL
        // admin.username -> ADMIN_USERNAME - Есть ли на компьютере переменная окружения BASEAPIURL?
        String envKey = key.toUpperCase().replace('.', '_');

        String envValue = System.getenv(envKey);
        if (envValue != null) {
            return envValue;
        }

        // Конфиг, который был изначально, до переделывания этого класса для базы данных
        // ПРИОРИТЕТ 3 - это config.properties - Тогда посмотрю в config.properties
        return INSTANCE.properties.getProperty(key);
    }
}