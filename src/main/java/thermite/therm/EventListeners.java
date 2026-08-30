/*
 * Copyright (c) 2023 sparkierkan7
 * Modifications Copyright (c) 2026 NicDev-Studios
 * SPDX-License-Identifier: MIT
 */

package thermite.therm;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Objects;
import java.util.Random;

import static thermite.therm.ThermMod.modVersion;

public class EventListeners {
    private static final int TEMPERATURE_SAMPLE_INTERVAL = 20;
    private static final TemperatureService TEMPERATURE_SERVICE = new TemperatureService(ThermMod.PLATFORM);
    private static int temperatureSampleTick;

    public static void register() {

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {

            ServerState serverState = ThermMod.PLATFORM.getServerState(handler.player.getWorld().getServer());
            ThermPlayerState playerState = ThermMod.PLATFORM.getPlayerState(handler.player);

            if (!Objects.equals(serverState.worldVersion, modVersion)) {

                serverState.windTempModifierRange = 8;
                serverState.windRandomizeTick = 24000;
                serverState.worldVersion = modVersion;

                serverState.players.forEach((uuid, state) -> {
                    state.windTurbulence = 23;
                });

                serverState.markDirty();
                ThermMod.LOGGER.info("Updated Thermite ServerState.");

            }

        });

        ServerTickEvents.END_SERVER_TICK.register((server) -> {
            ServerState serverState = ThermMod.PLATFORM.getServerState(server);

            if (serverState.windRandomizeTick >= 24000) {
                serverState.windRandomizeTick = 0;

                Random rand = new Random();
                serverState.windPitch = 360*Math.PI/180;
                serverState.windYaw = rand.nextDouble(0, 360)*Math.PI/180;
                serverState.windTempModifier = rand.nextDouble(-serverState.windTempModifierRange, serverState.windTempModifierRange);
                serverState.precipitationWindModifier = rand.nextDouble(-serverState.windTempModifierRange, 0);

                serverState.markDirty();
                ThermMod.LOGGER.info("========WIND RANDOMIZED========");

            }
            serverState.windRandomizeTick += 1;

            temperatureSampleTick++;
            if (temperatureSampleTick >= TEMPERATURE_SAMPLE_INTERVAL) {
                temperatureSampleTick = 0;
                server.getPlayerManager().getPlayerList().stream()
                        .filter(player -> !player.isCreative() && !player.isSpectator())
                        .forEach(player -> temperatureTick(server, player));
            }

        });

    }

    private static void temperatureTick(MinecraftServer server, ServerPlayerEntity player) {
        TEMPERATURE_SERVICE.tick(server, player);
    }
}
