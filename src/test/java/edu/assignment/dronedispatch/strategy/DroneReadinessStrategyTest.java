
package edu.assignment.dronedispatch.strategy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class DroneReadinessStrategyTest {

    @Test
    void standardStrategyRequiresAtLeast30Percent() {
        DroneReadinessStrategy strategy =
                new StandardReadinessStrategy();

        assertTrue(strategy.isReady(30));
        assertFalse(strategy.isReady(29));
    }

    @Test
    void conservativeStrategyRequiresAtLeast60Percent() {
        DroneReadinessStrategy strategy =
                new ConservativeReadinessStrategy();

        assertTrue(strategy.isReady(60));
        assertFalse(strategy.isReady(59));
    }

    @Test
    void emergencyStrategyRequiresAtLeast15Percent() {
        DroneReadinessStrategy strategy =
                new EmergencyReadinessStrategy();

        assertTrue(strategy.isReady(15));
        assertFalse(strategy.isReady(14));
    }

    static Stream<Arguments> allStrategies() {
        return Stream.of(
                Arguments.of(
                        new StandardReadinessStrategy(), 30.0, true),
                Arguments.of(
                        new ConservativeReadinessStrategy(), 30.0, false),
                Arguments.of(
                        new EmergencyReadinessStrategy(), 15.0, true)
        );
    }

    @ParameterizedTest
    @MethodSource("allStrategies")
    void strategiesReturnExpectedResult(
            DroneReadinessStrategy strategy,
            double battery,
            boolean expected) {

        assertEquals(expected, strategy.isReady(battery));
    }

    @Test
    void selectorChoosesConservativeStrategy() {
        DroneReadinessSelector selector =
                new DroneReadinessSelector();

        assertInstanceOf(
                ConservativeReadinessStrategy.class,
                selector.selectStrategy(60));
    }

    @Test
    void selectorChoosesStandardStrategy() {
        DroneReadinessSelector selector =
                new DroneReadinessSelector();

        assertInstanceOf(
                StandardReadinessStrategy.class,
                selector.selectStrategy(45));
    }

    @Test
    void selectorChoosesEmergencyStrategy() {
        DroneReadinessSelector selector =
                new DroneReadinessSelector();

        assertInstanceOf(
                EmergencyReadinessStrategy.class,
                selector.selectStrategy(20));
    }

    @Test
    void contextCanSwitchStrategies() {
        DroneReadinessContext context =
                new DroneReadinessContext(
                        new StandardReadinessStrategy());

        assertTrue(context.isReady(40));

        context.setStrategy(new ConservativeReadinessStrategy());

        assertFalse(context.isReady(40));
    }
}
