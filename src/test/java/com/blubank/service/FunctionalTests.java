package com.github.mehrdad.falahati.service;

import com.github.mehrdad.falahati.acceptance.DriverChoice;
import com.github.mehrdad.falahati.acceptance.DriverChoiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.stream.Stream;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class FunctionalTests {

    private DriverAcceptanceService driverService;
    private DriverChoice driverChoice;

    public static class DriverConfig {
        final int driverId;
        final int delayTime;
        final boolean response;

        public DriverConfig(int driverId, int delayTime, boolean response) {
            this.driverId = driverId;
            this.delayTime = delayTime;
            this.response = response;
        }

        @Override
        public String toString() {
            return String.format("DriverConfig(driverId=%d, delayTime=%d, response=%s)", driverId, delayTime, response);
        }
    }

    private void addBehaviour(DriverConfig driverConfig) {
        Mockito.when(this.driverService.getDriverResponse(driverConfig.driverId)).thenAnswer((invocation) -> {
            Thread.sleep(driverConfig.delayTime);
            return driverConfig.response;
        });
    }

    @BeforeEach
    public void setUp() {
        this.driverService = Mockito.mock(DriverAcceptanceService.class);
        this.driverChoice = new DriverChoiceImpl(this.driverService);
    }

    private static Stream<Arguments> generateDriverConfigs() {
        return Stream.of(
                Arguments.of(
                        new DriverConfig(1, 100, true),
                        new DriverConfig(2, 10, true),
                        2
                ),
                Arguments.of(
                        new DriverConfig(1, 10, true),
                        new DriverConfig(2, 100, true),
                        1
                ),
                Arguments.of(
                        new DriverConfig(1, 10, false),
                        new DriverConfig(2, 100, true),
                        2
                ),
                Arguments.of(
                        new DriverConfig(1, 10, true),
                        new DriverConfig(2, 100, false),
                        1
                ),
                Arguments.of(
                        new DriverConfig(1, 10, true),
                        new DriverConfig(2, 60000, true),
                        1
                ),
                Arguments.of(
                        new DriverConfig(1, 10, true),
                        new DriverConfig(2, 60000, false),
                        1
                ),
                Arguments.of(
                        new DriverConfig(1, 60000, true),
                        new DriverConfig(2, 10, true),
                        2
                ),
                Arguments.of(
                        new DriverConfig(1, 60000, false),
                        new DriverConfig(2, 10, true),
                        2
                ),
                Arguments.of(
                        new DriverConfig(1, 50000, true),
                        new DriverConfig(2, 10, true),
                        2
                ),
                Arguments.of(
                        new DriverConfig(1, 50000, false),
                        new DriverConfig(2, 10, true),
                        2
                )
        );
    }

    private static Stream<Arguments> generateDriverConfigsBothReject() {
        return Stream.of(
                Arguments.of(
                        new DriverConfig(1, 100, false),
                        new DriverConfig(2, 10, false)
                ),
                Arguments.of(
                        new DriverConfig(1, 10, false),
                        new DriverConfig(2, 100, false)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("generateDriverConfigs")
    public void testDriverAccept(
            DriverConfig driverConfig1,
            DriverConfig driverConfig2,
            int expectedDriverIdAccept
    ) {
        this.addBehaviour(driverConfig1);
        this.addBehaviour(driverConfig2);

        Assertions.assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
            Assertions.assertEquals(
                    new Integer(expectedDriverIdAccept),
                    this.driverChoice.getDriverAcceptance(driverConfig1.driverId, driverConfig2.driverId)
            );
        });
    }

    @ParameterizedTest
    @MethodSource("generateDriverConfigsBothReject")
    public void testDriverReject(
            DriverConfig driverConfig1,
            DriverConfig driverConfig2
    ) {
        this.addBehaviour(driverConfig1);
        this.addBehaviour(driverConfig2);

        Assertions.assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
            Assertions.assertNull(
                    this.driverChoice.getDriverAcceptance(driverConfig1.driverId, driverConfig2.driverId)
            );
        });
    }
}