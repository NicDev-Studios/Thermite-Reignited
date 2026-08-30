package thermite.therm.core;

import static thermite.therm.core.EnvironmentSnapshot.Precipitation.RAIN;
import static thermite.therm.core.EnvironmentSnapshot.Precipitation.SNOW;
import static thermite.therm.core.TemperatureState.DamageType.BURN;
import static thermite.therm.core.TemperatureState.DamageType.FREEZE;
import static thermite.therm.core.TemperatureState.DamageType.NONE;

/** Shared, deterministic body-temperature calculation. */
public final class TemperatureEngine {
    public TemperatureState tick(
            TemperatureState previous,
            EnvironmentSnapshot environment,
            ThermConfigModel config
    ) {
        ClimateProfile climate = climate(environment.biomeTemperature(), config);
        double restingTemperature = climate.restingTemperature();

        if (environment.naturalDimension() && !environment.daytime()) {
            restingTemperature += climate.nightModifier();
        }
        if (environment.raining()) {
            if (environment.precipitation() == RAIN && environment.wet() && !environment.touchingWater()) {
                restingTemperature -= 8;
            } else if (environment.precipitation() == SNOW) {
                restingTemperature -= 8;
            }
        }

        restingTemperature += environment.equipmentHeat();
        restingTemperature += environment.nearbyHeat();
        restingTemperature -= environment.nearbyCold();
        restingTemperature += environment.fireplaces() * 14.0;
        restingTemperature += environment.windTemperature();
        restingTemperature += environment.statusEffectModifier();
        if (environment.touchingWater()) {
            restingTemperature -= 10;
        }

        short direction = clampToShort(restingTemperature - previous.temperature());
        double temperature = moveTowards(previous.temperature(), restingTemperature, config.temperatureStep());

        TemperatureState.DamageType damageType = NONE;
        int maximumDamageTick = previous.maximumDamageTick();
        if (temperature <= config.freezeThreshold() && temperature > config.extremeFreezeThreshold()) {
            damageType = FREEZE;
            maximumDamageTick = config.damageInterval();
        } else if (temperature <= config.extremeFreezeThreshold()) {
            damageType = FREEZE;
            maximumDamageTick = config.extremeDamageInterval();
        } else if (temperature >= config.burnThreshold() && temperature < config.extremeBurnThreshold()) {
            damageType = BURN;
            maximumDamageTick = config.damageInterval();
        } else if (temperature >= config.extremeBurnThreshold()) {
            damageType = BURN;
            maximumDamageTick = config.extremeDamageInterval();
        }

        int damageTick = damageType == NONE ? 0 : previous.damageTick();
        boolean canDamage = damageType != BURN || !environment.heatDamageImmune();
        boolean damageDue = false;
        if (damageType != NONE && canDamage) {
            damageTick++;
            if (damageTick >= maximumDamageTick) {
                damageTick = 0;
                damageDue = true;
            }
        }

        return new TemperatureState(
                temperature,
                restingTemperature,
                climate.minimumTemperature(),
                climate.maximumTemperature(),
                direction,
                damageType,
                damageTick,
                maximumDamageTick,
                damageDue
        );
    }

    private static double moveTowards(double current, double target, double step) {
        if (Math.round(target) > Math.round(current)) {
            return current + step;
        }
        if (Math.round(target) < Math.round(current)) {
            return current - step;
        }
        return current;
    }

    private static short clampToShort(double value) {
        return (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, value));
    }

    private static ClimateProfile climate(float biomeTemperature, ThermConfigModel config) {
        if (biomeTemperature < 0.0f) {
            return new ClimateProfile(0, 80, config.frigidClimateTemperature(), -10);
        }
        if (biomeTemperature < 0.31f) {
            return new ClimateProfile(0, 100, config.coldClimateTemperature(), -10);
        }
        if (biomeTemperature < 0.9f) {
            return new ClimateProfile(0, 100, config.temperateClimateTemperature(), -10);
        }
        if (biomeTemperature < 2.0f) {
            return new ClimateProfile(40, 120, config.hotClimateTemperature(), -8);
        }
        return new ClimateProfile(40, 120, config.aridClimateTemperature(), -15);
    }

    private record ClimateProfile(
            double minimumTemperature,
            double maximumTemperature,
            double restingTemperature,
            double nightModifier
    ) {
    }
}
