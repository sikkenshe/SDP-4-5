
package edu.assignment.dronedispatch.strategy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class WeatherStrategyTest {

    private static final String DESTINATION = "Test destination";

    private static WeatherDataSource source(
            boolean storm, double wind, int visibility) {

        return new FixedWeatherDataSource(
                new WeatherConditions(storm, wind, visibility)
        );
    }

    // Test 1: safe weather passes the basic strategy.
    @Test
    void safeStrategyAcceptsSafeWeather() {
        WeatherStrategy strategy =
                new SafeWeatherStrategy(source(false, 10, 5000));

        assertTrue(strategy.isSafe(DESTINATION));
    }

    // Test 2: storm is rejected.
    @Test
    void safeStrategyRejectsStorm() {
        WeatherStrategy strategy =
                new SafeWeatherStrategy(source(true, 10, 5000));

        assertFalse(strategy.isSafe(DESTINATION));
    }

    // Test 3: wind strategy checks its wind limit.
    @Test
    void windStrategyRejectsStrongWind() {
        WeatherStrategy strategy =
                new WindAwareWeatherStrategy(
                        source(false, 35, 5000), 30);

        assertFalse(strategy.isSafe(DESTINATION));
    }

    @Test
    void windStrategyAcceptsWindWithinLimit() {
        WeatherStrategy strategy =
                new WindAwareWeatherStrategy(
                        source(false, 20, 5000), 30);

        assertTrue(strategy.isSafe(DESTINATION));
    }

    // Test 4: visibility strategy checks visibility.
    @Test
    void visibilityStrategyRejectsPoorVisibility() {
        WeatherStrategy strategy =
                new VisibilityWeatherStrategy(
                        source(false, 10, 800), 1000);

        assertFalse(strategy.isSafe(DESTINATION));
    }

    @Test
    void visibilityStrategyAcceptsGoodVisibility() {
        WeatherStrategy strategy =
                new VisibilityWeatherStrategy(
                        source(false, 10, 2000), 1000);

        assertTrue(strategy.isSafe(DESTINATION));
    }

    // Test 5: one parameterized test covers every strategy.
    static Stream<Arguments> weatherStrategies() {
        WeatherDataSource data = source(false, 35, 800);

        return Stream.of(
                Arguments.of(
                        new SafeWeatherStrategy(data), true),
                Arguments.of(
                        new WindAwareWeatherStrategy(data, 30), false),
                Arguments.of(
                        new VisibilityWeatherStrategy(data, 1000), false)
        );
    }

    @ParameterizedTest
    @MethodSource("weatherStrategies")
    void allStrategiesReturnExpectedResult(
            WeatherStrategy strategy, boolean expected) {

        assertEquals(expected, strategy.isSafe(DESTINATION));
    }

    // Test 6: selector chooses a strategy for each rule.
    @Test
    void selectorChoosesSafeStrategyForStorm() {
        WeatherStrategySelector selector =
                new WeatherStrategySelector(source(true, 10, 5000));

        assertInstanceOf(
                SafeWeatherStrategy.class,
                selector.selectStrategy(DESTINATION));
    }

    @Test
    void selectorChoosesWindStrategyForStrongWind() {
        WeatherStrategySelector selector =
                new WeatherStrategySelector(source(false, 35, 5000));

        assertInstanceOf(
                WindAwareWeatherStrategy.class,
                selector.selectStrategy(DESTINATION));
    }

    @Test
    void selectorChoosesVisibilityStrategyForPoorVisibility() {
        WeatherStrategySelector selector =
                new WeatherStrategySelector(source(false, 10, 800));

        assertInstanceOf(
                VisibilityWeatherStrategy.class,
                selector.selectStrategy(DESTINATION));
    }

    @Test
    void selectorChoosesSafeStrategyForNormalWeather() {
        WeatherStrategySelector selector =
                new WeatherStrategySelector(source(false, 10, 5000));

        assertInstanceOf(
                SafeWeatherStrategy.class,
                selector.selectStrategy(DESTINATION));
    }

    // Test 7: context supports runtime strategy switching.
    @Test
    void contextCanSwitchStrategies() {
        WeatherDataSource data = source(false, 35, 800);

        WeatherCheckContext context =
                new WeatherCheckContext(new SafeWeatherStrategy(data));

        assertTrue(context.isSafe(DESTINATION));

        context.setStrategy(new WindAwareWeatherStrategy(data, 30));

        assertFalse(context.isSafe(DESTINATION));
    }
}
