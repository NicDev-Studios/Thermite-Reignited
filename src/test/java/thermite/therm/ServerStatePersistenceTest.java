/*
 * Copyright (c) 2023 sparkierkan7
 * Modifications Copyright (c) 2026 NicDev-Studios
 * SPDX-License-Identifier: MIT
 */

package thermite.therm;

import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ServerStatePersistenceTest {
    @Test
    void worldAndPlayerStateRoundTripThroughNbt() {
        UUID playerId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        ServerState original = new ServerState();
        original.season = 3;
        original.seasonTick = 240;
        original.currentSeasonTick = 48000L;
        original.windPitch = 1.25;
        original.windYaw = -0.5;
        original.windRandomizeTick = 17;
        original.windTempModifierRange = 9.0;
        original.windTempModifier = -2.5;
        original.precipitationWindModifier = 0.75;

        ThermPlayerState player = new ThermPlayerState();
        player.testplayerint = 99;
        player.temp = 61.5;
        player.tempRate = -0.25;
        player.restingTemp = 70;
        player.minTemp = 40;
        player.maxTemp = 120;
        player.damageType = "burn";
        player.damageTick = 2;
        player.maxDamageTick = 3;
        player.searchFireplaceTick = 8;
        player.baseWindTemp = 4.5;
        player.windTemp = 3.25;
        player.windTurbulence = 1.5;
        original.players.put(playerId, player);

        ServerState restored = ServerState.createFromNbt(original.writeNbt(new NbtCompound()));

        assertEquals(original.season, restored.season);
        assertEquals(original.seasonTick, restored.seasonTick);
        assertEquals(original.currentSeasonTick, restored.currentSeasonTick);
        assertEquals(original.windPitch, restored.windPitch);
        assertEquals(original.windYaw, restored.windYaw);
        assertEquals(original.windRandomizeTick, restored.windRandomizeTick);
        assertEquals(original.windTempModifierRange, restored.windTempModifierRange);
        assertEquals(original.windTempModifier, restored.windTempModifier);
        assertEquals(original.precipitationWindModifier, restored.precipitationWindModifier);

        ThermPlayerState restoredPlayer = restored.players.get(playerId);
        assertNotNull(restoredPlayer);
        assertEquals(player.testplayerint, restoredPlayer.testplayerint);
        assertEquals(player.temp, restoredPlayer.temp);
        assertEquals(player.tempRate, restoredPlayer.tempRate);
        assertEquals(player.restingTemp, restoredPlayer.restingTemp);
        assertEquals(player.minTemp, restoredPlayer.minTemp);
        assertEquals(player.maxTemp, restoredPlayer.maxTemp);
        assertEquals(player.damageType, restoredPlayer.damageType);
        assertEquals(player.damageTick, restoredPlayer.damageTick);
        assertEquals(player.maxDamageTick, restoredPlayer.maxDamageTick);
        assertEquals(player.searchFireplaceTick, restoredPlayer.searchFireplaceTick);
        assertEquals(player.baseWindTemp, restoredPlayer.baseWindTemp);
        assertEquals(player.windTemp, restoredPlayer.windTemp);
        assertEquals(player.windTurbulence, restoredPlayer.windTurbulence);
    }
}
