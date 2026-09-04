package common;

import apiTests.iteration2_senior.models.AdminUsersResponse;
import apiTests.iteration2_senior.skelethon.endpoints.Endpoint;
import apiTests.iteration2_senior.skelethon.requests.ValidatedCrudRequester;
import apiTests.iteration2_senior.specs.RequestSpecs;
import apiTests.iteration2_senior.specs.ResponseSpecs;
import apiTests.iteration2_senior.steps.AdminStep;
import common.annotations.APIVersion;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.PostDiscoveryFilter;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;


public class DockerTestSuiteTest {
    private static final String FILE_DB = "infra/docker_compose/docker-compose.yml";
    private static final String FILE_OLD = "infra/docker_compose/docker-compose-without-db.yml";
    private static final String FILE_FROUD = "infra/docker_compose/docker-compose-fraud.yml";
    private static final String BACKEND_CHECK = "http://localhost:4111/actuator/health";
    private static final long WAIT_DURATION = 30_000;
    private static final String VALIDATION_FIX = "with_validation_fix";
    private static final String DATABASE_FIX = "with_database_with_fix";
    private static final String FRAUD_FIX = "with_fraud_check_with_approve";
    private static final String API_ITERATION_1 = "apiTests.iteration1_senior";
    private static final String API_ITERATION_2 = "apiTests.iteration2_senior";
    private static final String UI_ITERATION_1 = "uiTests.iteration1_senior";
    private static final String UI_ITERATION_2 = "uiTests.iteration2_senior";

    @Test
    void runAllTests() {
        int exitCode = 0;
        exitCode |= runBackendGroup(VALIDATION_FIX);
        exitCode |= runBackendGroup(DATABASE_FIX);
        exitCode |= runBackendGroup(FRAUD_FIX);
        if (exitCode != 0) {
            throw new AssertionError("One or more backend test groups failed");
        }
    }

    private static void runCleanup() {
        System.out.println();
        System.out.println("==============================================");
        System.out.println("CLEANUP");
        System.out.println("==============================================");

        AdminStep.deleteUsers();

        List<AdminUsersResponse> remainingUsers = new ValidatedCrudRequester<AdminUsersResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USERS_GET,
                ResponseSpecs.requestReturnsOk())
                .getAll(AdminUsersResponse[].class);

        if (!remainingUsers.isEmpty()) {
            throw new AssertionError("Cleanup failed. Remaining users: " + remainingUsers);
        }
        System.out.println("Cleanup completed successfully");
    }

    private static void runDatabaseCleanup() {
        System.out.println();
        System.out.println("==============================================");
        System.out.println("DATABASE CLEANUP");
        System.out.println("==============================================");

        List<AdminUsersResponse> users = new ValidatedCrudRequester<AdminUsersResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USERS_GET,
                ResponseSpecs.requestReturnsOk())
                .getAll(AdminUsersResponse[].class);

        for (AdminUsersResponse user : users) {
            if (!"ADMIN".equals(user.getRole())) {
                AdminStep.deleteUser(user.getId());
            }
        }

        List<AdminUsersResponse> remainingUsers = new ValidatedCrudRequester<AdminUsersResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USERS_GET,
                ResponseSpecs.requestReturnsOk())
                .getAll(AdminUsersResponse[].class);

        if (remainingUsers.size() != 1 || !"ADMIN".equals(remainingUsers.get(0).getRole())) {
            throw new AssertionError("Database cleanup failed. Remaining users: " + remainingUsers);
        }
        System.out.println("Database cleanup completed successfully");
    }


    private static int runBackendGroup(String backendVersion) {
        String composeFile = getComposeFile(backendVersion);

        System.out.println();
        System.out.println("==============================================");
        System.out.println("START BACKEND");
        System.out.println("Version: " + backendVersion);
        System.out.println("Compose: " + composeFile);
        System.out.println("==============================================");

        try {
            dockerUp(composeFile, backendVersion);
            waitForBackend();

            System.out.println();
            System.out.println("Starting tests for backend: " + backendVersion);

            int result = runTests(backendVersion);

            if (result == 0) {
                System.out.println();
                System.out.println("Checking backend before cleanup...");

                if (!isBackendAvailable()) {
                    throw new RuntimeException("Backend is NOT available before cleanup");
                }

                System.out.println("Backend is still UP");

                if (VALIDATION_FIX.equals(backendVersion)) {
                    runCleanup();
                } else if (DATABASE_FIX.equals(backendVersion) || FRAUD_FIX.equals(backendVersion)) {
                    runDatabaseCleanup();
                }
            }
            return result;
        } catch (Exception e) {
            System.err.println();
            System.err.println("Backend group failed: " + backendVersion);
            e.printStackTrace();
            return 1;
        } finally {
            System.out.println();
            System.out.println("Stopping backend: " + backendVersion);
            safeDockerDown(composeFile, backendVersion);
            System.out.println("Backend stopped: " + backendVersion);
        }
    }


    private static String getComposeFile(String backendVersion) {
        if (VALIDATION_FIX.equals(backendVersion)) {
            return FILE_OLD;
        }

        if (DATABASE_FIX.equals(backendVersion)) {
            return FILE_DB;
        }

        if (FRAUD_FIX.equals(backendVersion)) {
            return FILE_FROUD;
        }

        throw new IllegalArgumentException("Backend version does not exist: " + backendVersion);
    }


    private static void dockerUp(String composeFile, String backendVersion) {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "docker",
                "compose",
                "-f",
                composeFile,
                "up",
                "-d"
        );

        processBuilder
                .environment()
                .put("BACKEND_VERSION", backendVersion);

        processBuilder.inheritIO();
        runProcess(processBuilder);
    }

    private static void dockerDown(String composeFile, String backendVersion) {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "docker",
                "compose",
                "-f",
                composeFile,
                "down",
                "-v",
                "--remove-orphans"
        );

        processBuilder
                .environment()
                .put("BACKEND_VERSION", backendVersion);

        processBuilder.inheritIO();
        runProcess(processBuilder);
    }

    private static void safeDockerDown(String composeFile, String backendVersion) {
        try {
            dockerDown(composeFile, backendVersion);
        } catch (Exception e) {
            System.err.println("Could not stop compose: " + composeFile);
            e.printStackTrace();
        }
    }

    private static void waitForBackend() throws InterruptedException {
        System.out.println("Waiting for backend...");

        long timeout = System.currentTimeMillis() + WAIT_DURATION;

        while (System.currentTimeMillis() < timeout) {
            if (isBackendAvailable()) {
                System.out.println("Backend is UP");
                return;
            }
            Thread.sleep(1000);
        }
        throw new RuntimeException("Backend did not become available within " + WAIT_DURATION + " ms");
    }

    private static boolean isBackendAvailable() {
        try {
            HttpURLConnection connection = (HttpURLConnection) URI
                    .create(BACKEND_CHECK)
                    .toURL()
                    .openConnection();

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

    private static int runTests(String backendVersion) {
        PostDiscoveryFilter filter = createBackendFilter(backendVersion);

        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder
                .request()
                .selectors(
                        selectPackage(API_ITERATION_1),
                        selectPackage(API_ITERATION_2),
                        selectPackage(UI_ITERATION_1),
                        selectPackage(UI_ITERATION_2)
                )
                .filters(filter)
                .build();

        SummaryGeneratingListener summaryListener = new SummaryGeneratingListener();

        Launcher launcher = LauncherFactory.create();
        launcher.registerTestExecutionListeners(summaryListener);
        launcher.execute(request);

        var summary = summaryListener.getSummary();
        summary.printTo(new PrintWriter(System.out));

        if (summary.getTestsFoundCount() == 0) {
            System.out.println();
            System.out.println("NO TESTS FOUND for backend: " + backendVersion);
            return 1;
        }

        if (summary.getFailures().isEmpty()) {
            System.out.println();
            System.out.println("Tests PASSED for backend: " + backendVersion);
            return 0;
        }

        System.out.println();
        System.out.println("Tests FAILED for backend: " + backendVersion);

        summary.getFailures().forEach(failure -> {
            System.err.println();
            System.err.println("========== TEST FAILURE ==========");
            System.err.println("Test: " + failure.getTestIdentifier().getDisplayName());
            System.err.println("Exception: " + failure.getException());
            failure.getException().printStackTrace(System.err);
        });

        return 1;
    }

    private static PostDiscoveryFilter createBackendFilter(String backendVersion) {
        return testDescriptor -> {
            Optional<ClassSource> classSource = findClassSource(testDescriptor);

            if (classSource.isEmpty()) {
                return FilterResult.included("No class source");
            }

            Class<?> testClass = classSource
                    .get()
                    .getJavaClass();

            APIVersion annotation = testClass.getAnnotation(APIVersion.class);

            if (annotation != null && backendVersion.equals(annotation.value())) {
                return FilterResult.included("API version matches: " + backendVersion);
            }
            return FilterResult.excluded("API version does not match: " + backendVersion);
        };
    }

    private static Optional<ClassSource> findClassSource(TestDescriptor testDescriptor) {
        TestDescriptor current = testDescriptor;

        while (current != null) {
            Optional<ClassSource> classSource = current
                    .getSource()
                    .filter(ClassSource.class::isInstance)
                    .map(ClassSource.class::cast);
            if (classSource.isPresent()) {
                return classSource;
            }
            current = current.getParent().orElse(null);
        }
        return Optional.empty();
    }

    private static void runProcess(ProcessBuilder processBuilder) {
        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("Process failed with exit code: " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Process was interrupted", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute process", e);
        }
    }
}