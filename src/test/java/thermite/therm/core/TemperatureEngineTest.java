package thermite.therm.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemperatureEngineTest {
    private final TemperatureEngine engine = new TemperatureEngine();
    private final ThermConfigModel config = new ThermConfigModel(
            25, 30, 50, 55, 70,
            35, 25, 65, 75,
            3, 2, 0.25
    );

    @Test
    void temperateDayMovesTowardFifty() {
        TemperatureState result = engine.tick(
                TemperatureState.initial(40, 0),
                environment(0.8f),
                config
        );

        assertEquals(50, result.restingTemperature());
        assertEquals(40.25, result.temperature());
        assertTrue(result.direction() > 0);
    }

    @Test
    void nightRainAndWaterAreAppliedByTheSharedCore() {
        EnvironmentSnapshot environment = new EnvironmentSnapshot(
                0.8f,
                true,
                false,
                EnvironmentSnapshot.Precipitation.RAIN,
                true,
                true,
                true,
                0,
                0,
                0,
                0,
                0,
                0,
                false
        );

        TemperatureState result = engine.tick(TemperatureState.initial(50, 0), environment, config);

        // Wet rain is not counted twice while submerged: 50 - 10 night - 10 water.
        assertEquals(30, result.restingTemperature());
        assertEquals(49.75, result.temperature());
    }

    @Test
    void extremeColdSchedulesDamageDeterministically() {
        TemperatureState first = engine.tick(TemperatureState.initial(20, 0), environment(-1), config);
        TemperatureState second = engine.tick(first, environment(-1), config);

        assertEquals(TemperatureState.DamageType.FREEZE, second.damageType());
        assertTrue(second.damageDue());
        assertEquals(0, second.damageTick());
    }

    @Test
    void fireImmunityStopsBurnDamageCounter() {
        EnvironmentSnapshot immuneHeat = new EnvironmentSnapshot(
                2.0f,
                false,
                true,
                EnvironmentSnapshot.Precipitation.NONE,
                false,
                false,
                false,
                0,
                0,
                0,
                0,
                0,
                0,
                true
        );

        TemperatureState result = engine.tick(TemperatureState.initial(80, 0), immuneHeat, config);

        assertEquals(TemperatureState.DamageType.BURN, result.damageType());
        assertEquals(0, result.damageTick());
        assertFalse(result.damageDue());
    }

    private static EnvironmentSnapshot environment(float biomeTemperature) {
        return new EnvironmentSnapshot(
                biomeTemperature,
                true,
                true,
                EnvironmentSnapshot.Precipitation.NONE,
                false,
                false,
                false,
                0,
                0,
                0,
                0,
                0,
                0,
                false
        );
    }
}
