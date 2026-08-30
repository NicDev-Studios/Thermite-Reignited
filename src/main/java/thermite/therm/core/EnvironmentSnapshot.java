package thermite.therm.core;

/**
 * Minecraft-independent view of everything that can influence one temperature
 * sample. Platform adapters are responsible for producing this value.
 */
public record EnvironmentSnapshot(
        float biomeTemperature,
        boolean naturalDimension,
        boolean daytime,
        Precipitation precipitation,
        boolean raining,
        boolean wet,
        boolean touchingWater,
        double equipmentHeat,
        double nearbyHeat,
        double nearbyCold,
        int fireplaces,
        double windTemperature,
        double statusEffectModifier,
        boolean heatDamageImmune
) {
    public enum Precipitation {
        NONE,
        RAIN,
        SNOW
    }
}
