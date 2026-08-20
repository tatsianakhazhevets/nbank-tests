package common.extensions;

import common.annotations.APIVersion;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.net.HttpURLConnection;
import java.net.URI;

public class APIVersionExtension implements BeforeAllCallback, AfterAllCallback {

    private static final String FILE_DB = "infra/docker_compose/docker-compose.yml";
    private static final String FILE_OLD = "infra/docker_compose/docker-compose-without-db.yml";
    private static final String BACKEND_URL = "http://localhost:4111/actuator/health";
    private static final long WAIT_DURATION = 30_000;
    private String backendVersion;
    private String composeFile;

    private String getComposeFile(String backendVersion) {
        if ("with_validation_fix".equals(backendVersion)) {
            return FILE_OLD;
        }

        if ("with_database_with_fix".equals(backendVersion)) {
            return FILE_DB;
        }

        throw new IllegalArgumentException("Backed version is not exist " + backendVersion);
    }

    private boolean isBackendAvailable() {
        try {
            HttpURLConnection connection = (HttpURLConnection) URI.create(BACKEND_URL).toURL().openConnection();
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(1000);
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            return responseCode == 200;
        } catch (Exception e) {
            return false;
        }
    }

    private void waitForBackend() throws InterruptedException {
        System.out.println("Waiting for backend");

        long timeout = System.currentTimeMillis() + WAIT_DURATION;

        while (System.currentTimeMillis() < timeout) {
            if (isBackendAvailable()) {
                System.out.println("Backend is up");
                return;
            }
            Thread.sleep(1000);
        }

        throw new RuntimeException("Backend is down");
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        APIVersion annotation = context.getRequiredTestClass().getAnnotation(APIVersion.class);

        if (annotation == null) {
            throw new IllegalStateException("Class does not have annotation APIVersion " + getClass().getPackageName());
        }

        backendVersion = annotation.value();
        composeFile = getComposeFile(backendVersion);

        System.out.println("Backend version: " + backendVersion + "\nCompose file: " + composeFile);

        ProcessBuilder processBuilder = new ProcessBuilder(
                "docker",
                "compose",
                "-f",
                composeFile,
                "up",
                "-d"
        );

        processBuilder.environment().put("BACKEND_VERSION", backendVersion);
        processBuilder.inheritIO();
        Process process = processBuilder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Failed to start " + composeFile);
        }
        System.out.println(composeFile + " has been started");

        waitForBackend();
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        System.out.println("Start to stop " + composeFile);

        ProcessBuilder processBuilder = new ProcessBuilder(
                "docker",
                "compose",
                "-f",
                composeFile,
                "down"
        );

        processBuilder.environment().put("BACKEND_VERSION", backendVersion);
        processBuilder.inheritIO();
        Process process = processBuilder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Failed to stop docker-compose.yml");
        }
        System.out.println(composeFile + " has been stopped");
    }
}