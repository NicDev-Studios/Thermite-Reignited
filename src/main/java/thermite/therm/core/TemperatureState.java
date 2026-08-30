/*
 * Copyright (c) 2023 sparkierkan7
 * Modifications Copyright (c) 2026 NicDev-Studios
 * SPDX-License-Identifier: MIT
 */

package thermite.therm.core;

/** Immutable result of one shared temperature-engine step. */
public record TemperatureState(
        double temperature,
        double restingTemperature,
        double minimumTemperature,
        double maximumTemperature,
        short direction,
        DamageType damageType,
        int damageTick,
        int maximumDamageTick,
        boolean damageDue
) {
    public enum DamageType {
        NONE,
        FREEZE,
        BURN
    }

    public static TemperatureState initial(double temperature, int damageTick) {
        return new TemperatureState(
                temperature,
                temperature,
                -400,
                400,
                (short) 0,
                DamageType.NONE,
                damageTick,
                10,
                false
        );
    }
}
