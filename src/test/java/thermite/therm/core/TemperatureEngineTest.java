/*
 * Copyright (c) 2023 sparkierkan7
 * Modifications Copyright (c) 2026 NicDev-Studios
 * SPDX-License-Identifier: MIT
 */

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

    @Test
    void climateBandsUseTheirConfiguredRestingTemperatures() {
        assertEquals(25, engine.tick(TemperatureState.initial(50, 0), environment(-1.0f), config).restingTemperature());
        assertEquals(30, engine.tick(TemperatureState.initial(50, 0), environment(0.2f), config).restingTemperature());
        assertEquals(50, engine.tick(TemperatureState.initial(50, 0), environment(0.8f), config).restingTemperature());
        assertEquals(55, engine.tick(TemperatureState.initial(50, 0), environment(1.5f), config).restingTemperature());
        assertEquals(70, engine.tick(TemperatureState.initial(50, 0), environment(2.0f), config).restingTemperature());
    }

    @Test
    void nearbyHeatColdFireplacesWindAndEffectsAreCombined() {
        EnvironmentSnapshot modifiers = new EnvironmentSnapshot(
                0.8f,
                true,
                true,
                EnvironmentSnapshot.Precipitation.NONE,
                false,
                false,
                false,
                3,
                4,
                2,
                1,
                5,
                6,
                false
        );

        TemperatureState result = engine.tick(TemperatureState.initial(50, 0), modifiers, config);

        assertEquals(80, result.restingTemperature());
        assertEquals(50.25, result.temperature());
        assertTrue(result.direction() > 0);
    }

    @Test
    void snowOnlyCoolsWhenItIsActuallyRaining() {
        EnvironmentSnapshot drySnow = new EnvironmentSnapshot(
                0.8f,
                true,
                true,
                EnvironmentSnapshot.Precipitation.SNOW,
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
        EnvironmentSnapshot fallingSnow = new EnvironmentSnapshot(
                0.8f,
                true,
                true,
                EnvironmentSnapshot.Precipitation.SNOW,
                true,
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

        assertEquals(50, engine.tick(TemperatureState.initial(50, 0), drySnow, config).restingTemperature());
        assertEquals(42, engine.tick(TemperatureState.initial(50, 0), fallingSnow, config).restingTemperature());
    }

    @Test
    void normalDamageUsesConfiguredIntervalAndResetsAfterDamage() {
        TemperatureState state = TemperatureState.initial(34, 0);
        EnvironmentSnapshot cold = environment(-1.0f);

        state = engine.tick(state, cold, config);
        assertFalse(state.damageDue());
        state = engine.tick(state, cold, config);
        assertFalse(state.damageDue());
        state = engine.tick(state, cold, config);

        assertEquals(TemperatureState.DamageType.FREEZE, state.damageType());
        assertTrue(state.damageDue());
        assertEquals(0, state.damageTick());
        assertEquals(3, state.maximumDamageTick());
    }

    @Test
    void movingBackIntoSafeRangeClearsDamageState() {
        ThermConfigModel fastConfig = new ThermConfigModel(
                25, 30, 50, 55, 70,
                35, 25, 65, 75,
                3, 2, 20
        );

        TemperatureState frozen = engine.tick(
                TemperatureState.initial(50, 0),
                environment(-1.0f),
                fastConfig
        );
        TemperatureState safe = engine.tick(
                frozen,
                environment(0.8f),
                fastConfig
        );

        assertEquals(TemperatureState.DamageType.NONE, safe.damageType());
        assertEquals(0, safe.damageTick());
        assertFalse(safe.damageDue());
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
