package common_iteration2.extensions;

import apiTests.iteration2_senior.models.FraudCheckMockResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import common_iteration2.annotations.MyFraudCheckMock;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class MyFraudCheckMockExtension implements BeforeEachCallback, AfterEachCallback {
    private WireMockServer wireMockServer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void beforeEach(ExtensionContext context) throws JsonProcessingException {
        MyFraudCheckMock mockConfig = context.getTestMethod()
                .map(method -> method.getAnnotation(MyFraudCheckMock.class))
                .orElseGet(() -> context.getTestClass()
                        .map(clazz -> clazz.getAnnotation(MyFraudCheckMock.class))
                        .orElse(null));

        if (mockConfig != null) {
            setupWireMock(mockConfig);
        }
    }

    private void setupWireMock(MyFraudCheckMock config) throws JsonProcessingException {

        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(config.port()));
        wireMockServer.start();
        WireMock.configureFor("0.0.0.0", config.port());

        FraudCheckMockResponse response = new FraudCheckMockResponse(
                config.status(),
                config.decision(),
                config.riskScore(),
                config.reason(),
                config.requiresManualReview(),
                config.additionalVerificationRequired()
        );

        String responseBody = objectMapper.writeValueAsString(response);

        if (config.testType().equals("positive")) {
            stubFor(post(urlPathMatching(config.endpoint()))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(responseBody)));
        } else {
            stubFor(post(urlPathMatching(config.endpoint()))
                    .willReturn(aResponse()
                            .withStatus(500)
                            .withHeader("Content-Type", "application/json")));
        }
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