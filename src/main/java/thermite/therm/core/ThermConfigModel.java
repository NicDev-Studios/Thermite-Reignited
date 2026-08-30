/*
 * Copyright (c) 2023 sparkierkan7
 * Modifications Copyright (c) 2026 NicDev-Studios
 * SPDX-License-Identifier: MIT
 */

package thermite.therm.core;

/** Values used by the shared temperature engine, detached from the config library. */
public record ThermConfigModel(
        double frigidClimateTemperature,
        double coldClimateTemperature,
        double temperateClimateTemperature,
        double hotClimateTemperature,
        double aridClimateTemperature,
        int freezeThreshold,
        int extremeFreezeThreshold,
        int burnThreshold,
        int extremeBurnThreshold,
        int damageInterval,
        int extremeDamageInterval,
        double temperatureStep
) {
}
