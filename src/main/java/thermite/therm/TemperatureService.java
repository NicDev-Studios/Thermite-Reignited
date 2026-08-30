/*
 * Copyright (c) 2023 sparkierkan7
 * Modifications Copyright (c) 2026 NicDev-Studios
 * SPDX-License-Identifier: MIT
 */

package thermite.therm;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import thermite.therm.config.ThermConfigAdapter;
import thermite.therm.core.EnvironmentSnapshot;
import thermite.therm.core.TemperatureEngine;
import thermite.therm.core.TemperatureState;
import thermite.therm.platform.ThermPlatform;

/** Coordinates server-authoritative samples between the core and the platform adapter. */
public final class TemperatureService {
    private final TemperatureEngine engine = new TemperatureEngine();
    private final ThermPlatform platform;

    public TemperatureService(ThermPlatform platform) {
        this.platform = platform;
    }

    public void tick(MinecraftServer server, ServerPlayerEntity player) {
        ServerState serverState = platform.getServerState(server);
        ThermPlayerState persisted = platform.getPlayerState(player);
        EnvironmentSnapshot environment = platform.captureEnvironment(server, player, serverState, persisted);
        TemperatureState previous = new TemperatureState(
                persisted.temp,
                persisted.restingTemp,
                persisted.minTemp,
                persisted.maxTemp,
                (short) 0,
                parseDamageType(persisted.damageType),
                persisted.damageTick,
                persisted.maxDamageTick,
                false
        );
        TemperatureState next = engine.tick(previous, environment, ThermConfigAdapter.toModel(ThermMod.config));

        persisted.temp = next.temperature();
        persisted.restingTemp = next.restingTemperature();
        persisted.minTemp = next.minimumTemperature();
        persisted.maxTemp = next.maximumTemperature();
        persisted.damageType = switch (next.damageType()) {
            case FREEZE -> "freeze";
            case BURN -> "burn";
            case NONE -> "";
        };
        persisted.damageTick = next.damageTick();
        persisted.maxDamageTick = next.maximumDamageTick();

        platform.applyDamage(player, next);
        TemperatureState synchronizedState = next;
        if (player.getHealth() <= 0.0f) {
            persisted.temp = 50;
            persisted.damageTick = 0;
            synchronizedState = new TemperatureState(
                    50,
                    next.restingTemperature(),
                    next.minimumTemperature(),
                    next.maximumTemperature(),
                    next.direction(),
                    next.damageType(),
                    0,
                    next.maximumDamageTick(),
                    false
            );
        }
        platform.synchronize(player, serverState, synchronizedState);
        serverState.markDirty();
    }

    private static TemperatureState.DamageType parseDamageType(String value) {
        return switch (value) {
            case "freeze" -> TemperatureState.DamageType.FREEZE;
            case "burn" -> TemperatureState.DamageType.BURN;
            default -> TemperatureState.DamageType.NONE;
        };
    }
}
