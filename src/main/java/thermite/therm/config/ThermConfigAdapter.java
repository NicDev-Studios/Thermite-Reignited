/*
 * Copyright (c) 2023 sparkierkan7
 * Modifications Copyright (c) 2026 NicDev-Studios
 * SPDX-License-Identifier: MIT
 */

package thermite.therm.config;

import thermite.therm.ThermConfig;
import thermite.therm.core.ThermConfigModel;

/** Isolates CompleteConfig from the shared engine model. */
public final class ThermConfigAdapter {
    private ThermConfigAdapter() {
    }

    public static ThermConfigModel toModel(ThermConfig config) {
        return new ThermConfigModel(
                config.frigidClimateTemp,
                config.coldClimateTemp,
                config.temperateClimateTemp,
                config.hotClimateTemp,
                config.aridClimateTemp,
                config.freezeThreshold1,
                config.freezeThreshold2,
                config.burnThreshold1,
                config.burnThreshold2,
                config.temperatureDamageInterval,
                config.extremetemperatureDamageInterval,
                0.25
        );
    }
}
