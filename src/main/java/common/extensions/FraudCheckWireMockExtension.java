package common.extensions;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import common.annotations.FraudCheckMock;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Locale;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class FraudCheckWireMockExtension implements BeforeEachCallback, AfterEachCallback {
    private WireMockServer wireMockServer;

    @Override
    public void beforeEach(ExtensionContext context) {
        // Find the FraudCheckMock annotation on the test method or class
        FraudCheckMock mockConfig = context.getTestMethod()
                .map(method -> method.getAnnotation(FraudCheckMock.class))
                .orElseGet(() -> context.getTestClass()
                        .map(clazz -> clazz.getAnnotation(FraudCheckMock.class))
                        .orElse(null));

        if (mockConfig != null) {
            setupWireMock(mockConfig);
        }
    }

    //Вот тут поднятие Мока, настройка HTTP client
    private void setupWireMock(FraudCheckMock config) {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(config.port()));
        wireMockServer.start();
        WireMock.configureFor("0.0.0.0", config.port());
        //host 0.., так как мы хотим, чтобы стучались в сеть, где поднято приложение
        //wiremock будет доступен внутри сети, где поднято приложение

        // Create the response body based on annotation parameters
        //Это некоторая заглушка - ответ от фрод сервиса, из указанных полей формируется тело ответа
        String responseBody = String.format(Locale.US, "{\n" +  //структура ответа - как отвечает реальный фрауд сервис отличает
                        "  \"status\": \"%s\",\n" +
                        "  \"decision\": \"%s\",\n" +
                        "  \"riskScore\": %.1f,\n" +
                        "  \"reason\": \"%s\",\n" +
                        "  \"requiresManualReview\": %s,\n" +
                        "  \"additionalVerificationRequired\": %s\n" +
                        "}",
                //тут нужно сделать объект и серилизацию
                config.status(),
                config.decision(),
                config.riskScore(),
                config.reason(),
                config.requiresManualReview(),
                config.additionalVerificationRequired());

        // Mock the fraud detection service endpoint
        // Настройка непосредственно мока
        stubFor(post(urlPathMatching(config.endpoint()))
                //если вам пришел пост запрос на урл (который мы передадим бэку - используй этот урл как фрауд сервис)
                .willReturn(aResponse()
                        //возвращаем как респонс - 200, хэдэр + нужный воду
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));
    }

    @Override
    public void afterEach(ExtensionContext context) {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    public String getBaseUrl() {
        if (wireMockServer != null) {
            return "http://host.docker.internal:" + wireMockServer.port();
        }
        return null;
    }
}

// можно поднять wiremock внутри докер контейнера
//используем поднятие Wiremock не через докер, а нужно подключится к докер-сети приложения