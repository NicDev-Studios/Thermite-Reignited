/*
 * Copyright (c) 2023 sparkierkan7
 * Modifications Copyright (c) 2026 NicDev-Studios
 * SPDX-License-Identifier: MIT
 */

package thermite.therm.platform;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemGroups;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.biome.Biome;
import thermite.therm.ServerState;
import thermite.therm.ThermMod;
import thermite.therm.ThermPlayerState;
import thermite.therm.block.FireplaceBlock;
import thermite.therm.block.ThermBlocks;
import thermite.therm.core.EnvironmentSnapshot;
import thermite.therm.core.TemperatureState;
import thermite.therm.effect.ThermStatusEffects;
import thermite.therm.networking.ThermNetworkingPackets;
import thermite.therm.recipe.LeatherArmorWoolRecipe;

import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/** Fabric/Yarn implementation of the platform boundary for the 1.20.x adapters. */
public final class FabricThermPlatform implements ThermPlatform {
    @Override
    public void registerContent() {
        Registry.register(Registries.STATUS_EFFECT, ThermMod.id("cooling"), ThermStatusEffects.COOLING);

        Registry.register(Registries.ITEM, ThermMod.id("gold_sweet_berries"), ThermMod.GOLD_SWEET_BERRIES_ITEM);
        Registry.register(Registries.ITEM, ThermMod.id("ice_juice"), ThermMod.ICE_JUICE_ITEM);
        Registry.register(Registries.ITEM, ThermMod.id("thermometer"), ThermMod.THERMOMETER_ITEM);
        Registry.register(Registries.ITEM, ThermMod.id("wool_cloth"), ThermMod.WOOL_CLOTH_ITEM);
        Registry.register(Registries.ITEM, ThermMod.id("tester_item"), ThermMod.TESTER_ITEM);

        Registry.register(Registries.BLOCK, ThermMod.id("ice_box_empty"), ThermBlocks.ICE_BOX_EMPTY_BLOCK);
        Registry.register(Registries.BLOCK, ThermMod.id("ice_box_freezing"), ThermBlocks.ICE_BOX_FREEZING_BLOCK);
        Registry.register(Registries.BLOCK, ThermMod.id("ice_box_frozen"), ThermBlocks.ICE_BOX_FROZEN_BLOCK);
        Registry.register(Registries.BLOCK, ThermMod.id("fireplace"), ThermBlocks.FIREPLACE_BLOCK);
        Registry.register(Registries.BLOCK, ThermMod.id("smoke"), ThermBlocks.SMOKE_BLOCK);

        Registry.register(Registries.ITEM, ThermMod.id("ice_box_empty_item"), ThermMod.ICE_BOX_EMPTY_ITEM);
        Registry.register(Registries.ITEM, ThermMod.id("ice_box_freezing_item"), ThermMod.ICE_BOX_FREEZING_ITEM);
        Registry.register(Registries.ITEM, ThermMod.id("ice_box_frozen_item"), ThermMod.ICE_BOX_FROZEN_ITEM);
        Registry.register(Registries.ITEM, ThermMod.id("fireplace_item"), ThermMod.FIREPLACE_ITEM);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(entries -> {
            entries.add(ThermMod.GOLD_SWEET_BERRIES_ITEM);
            entries.add(ThermMod.ICE_JUICE_ITEM);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(ThermMod.THERMOMETER_ITEM));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
            entries.add(ThermMod.ICE_BOX_EMPTY_ITEM);
            entries.add(ThermMod.FIREPLACE_ITEM);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> entries.add(ThermMod.WOOL_CLOTH_ITEM));
    }

    @Override
    public void registerNetworking() {
        // Temperature sampling is deliberately server-driven. Only S2C state sync
        // remains part of the temperature protocol.
    }

    @Override
    public RecipeSerializer<LeatherArmorWoolRecipe> createLeatherArmorRecipeSerializer() {
        return RecipeSerializer.register(
                "crafting_special_leather_armor_wool",
                new SpecialRecipeSerializer<>(LeatherArmorWoolRecipe::new)
        );
    }

    @Override
    public ServerState getServerState(MinecraftServer server) {
        return ServerState.getServerState(server);
    }

    @Override
    public ThermPlayerState getPlayerState(ServerPlayerEntity player) {
        return ServerState.getPlayerState(player);
    }

    @Override
    public EnvironmentSnapshot captureEnvironment(
            MinecraftServer server,
            ServerPlayerEntity player,
            ServerState serverState,
            ThermPlayerState playerState
    ) {
        Biome biome = player.getWorld().getBiome(player.getBlockPos()).value();
        Biome.Precipitation biomePrecipitation = biome.getPrecipitation(player.getBlockPos());
        EnvironmentSnapshot.Precipitation precipitation = switch (biomePrecipitation) {
            case RAIN -> EnvironmentSnapshot.Precipitation.RAIN;
            case SNOW -> EnvironmentSnapshot.Precipitation.SNOW;
            default -> EnvironmentSnapshot.Precipitation.NONE;
        };

        AtomicInteger armorHeat = new AtomicInteger();
        addArmorHeat(player.getInventory().getArmorStack(0), ThermMod.config.bootTempItems, armorHeat);
        addArmorHeat(player.getInventory().getArmorStack(1), ThermMod.config.leggingTempItems, armorHeat);
        addArmorHeat(player.getInventory().getArmorStack(2), ThermMod.config.chestplateTempItems, armorHeat);
        addArmorHeat(player.getInventory().getArmorStack(3), ThermMod.config.helmetTempItems, armorHeat);

        double equipmentHeat = armorHeat.get()
                + configuredItemHeat(player.getMainHandStack(), ThermMod.config.heldTempItems)
                + configuredItemHeat(player.getOffHandStack(), ThermMod.config.heldTempItems);

        Vec3d position = player.getPos();
        double nearbyHeat = player.getWorld().getStatesInBox(Box.of(position, 4, 4, 4))
                .mapToDouble(this::configuredHeatingBlockValue)
                .sum();
        double nearbyCold = armorHeat.get() < 2
                ? player.getWorld().getStatesInBox(Box.of(position, 2, 3, 2))
                        .mapToDouble(this::configuredCoolingBlockValue)
                        .sum()
                : 0;

        if (playerState.searchFireplaceTick <= 0) {
            playerState.searchFireplaceTick = 4;
            playerState.fireplaces = (int) player.getWorld().getStatesInBox(Box.of(position, 12, 12, 12))
                    .filter(state -> state.isOf(ThermBlocks.FIREPLACE_BLOCK) && state.get(FireplaceBlock.LIT))
                    .count();
            updateWind(player, serverState, playerState, precipitation);
        }
        playerState.searchFireplaceTick--;

        double statusEffectModifier = 0;
        if (player.hasStatusEffect(ThermStatusEffects.COOLING)) {
            int amplifier = Objects.requireNonNull(player.getStatusEffect(ThermStatusEffects.COOLING)).getAmplifier();
            statusEffectModifier = -(10 + 10 * amplifier);
        }

        int fireProtection = fireProtectionLevel(player);
        int fireProtectionOverage = fireProtection - ThermMod.config.fireProtectionLevelCount;
        boolean protectedByArmor = fireProtection >= ThermMod.config.fireProtectionLevelCount
                && playerState.temp <= 70 + fireProtectionOverage;
        boolean heatDamageImmune = player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.FIRE_RESISTANCE)
                || protectedByArmor;

        return new EnvironmentSnapshot(
                biome.getTemperature(),
                player.getWorld().getDimension().natural(),
                player.getWorld().isDay(),
                precipitation,
                player.getWorld().isRaining(),
                player.isWet(),
                player.isTouchingWater(),
                equipmentHeat,
                nearbyHeat,
                nearbyCold,
                playerState.fireplaces,
                playerState.windTemp,
                statusEffectModifier,
                heatDamageImmune
        );
    }

    @Override
    public void applyDamage(ServerPlayerEntity player, TemperatureState state) {
        if (!state.damageDue()) {
            return;
        }
        if (ThermMod.config.temperatureDamageDecreasesSaturation) {
            player.getHungerManager().setSaturationLevel(0f);
        }
        if (state.damageType() == TemperatureState.DamageType.FREEZE) {
            player.damage(player.getWorld().getDamageSources().freeze(), ThermMod.config.hypothermiaDamage);
        } else if (state.damageType() == TemperatureState.DamageType.BURN) {
            player.damage(player.getWorld().getDamageSources().onFire(), ThermMod.config.hyperthermiaDamage);
        }
    }

    @Override
    public void synchronize(ServerPlayerEntity player, ServerState serverState, TemperatureState state) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeDouble(state.temperature());
        data.writeShort(state.direction());
        data.writeDouble(serverState.windPitch);
        data.writeDouble(serverState.windYaw);
        data.writeDouble(ServerState.getPlayerState(player).windTemp);
        ServerPlayNetworking.send(player, ThermNetworkingPackets.SEND_THERMPLAYERSTATE_S2C_PACKET_ID, data);
    }

    private static void addArmorHeat(ItemStack stack, Map<String, Integer> configuredItems, AtomicInteger total) {
        int configuredHeat = configuredItemHeat(stack, configuredItems);
        if (configuredHeat == 0) {
            return;
        }
        int wool = stack.getNbt() == null ? 0 : stack.getNbt().getInt("wool");
        total.addAndGet(configuredHeat + wool);
    }

    private static int configuredItemHeat(ItemStack stack, Map<String, Integer> configuredItems) {
        return configuredItems.getOrDefault(stack.getItem().toString(), 0);
    }

    private double configuredHeatingBlockValue(BlockState state) {
        int value = configuredBlockValue(state, ThermMod.config.heatingBlocks);
        if (value != 0 && (state.isOf(Blocks.CAMPFIRE) || state.isOf(Blocks.SOUL_CAMPFIRE))) {
            return state.get(CampfireBlock.LIT) ? value : 0;
        }
        return value;
    }

    private double configuredCoolingBlockValue(BlockState state) {
        return configuredBlockValue(state, ThermMod.config.coolingBlocks);
    }

    private static int configuredBlockValue(BlockState state, Map<String, Integer> configuredBlocks) {
        Integer exact = configuredBlocks.get(state.toString());
        if (exact != null) {
            return exact;
        }
        return configuredBlocks.getOrDefault(state.getBlock().toString(), 0);
    }

    private static int fireProtectionLevel(ServerPlayerEntity player) {
        int level = 0;
        for (ItemStack armor : player.getArmorItems()) {
            level += EnchantmentHelper.getLevel(Enchantments.FIRE_PROTECTION, armor);
        }
        return level;
    }

    private static void updateWind(
            ServerPlayerEntity player,
            ServerState serverState,
            ThermPlayerState playerState,
            EnvironmentSnapshot.Precipitation precipitation
    ) {
        if (!ThermMod.config.enableWind) {
            playerState.windTemp = 0;
            return;
        }
        boolean naturalDimension = player.getWorld().getDimension().natural();
        if (!ThermMod.config.multidimensionalWind && !naturalDimension) {
            playerState.windTemp = 0;
            return;
        }

        double calculatedWindTemperature = serverState.windTempModifier;
        if (player.getY() > 62) {
            double height = player.getY() - 62;
            calculatedWindTemperature -= height / (player.getY() <= 150 ? 7 : 8);
        }
        if (player.getWorld().isRaining()) {
            if (precipitation == EnvironmentSnapshot.Precipitation.RAIN) {
                calculatedWindTemperature += serverState.precipitationWindModifier;
            } else if (precipitation == EnvironmentSnapshot.Precipitation.SNOW) {
                calculatedWindTemperature += serverState.precipitationWindModifier * 1.3;
            }
        }
        playerState.baseWindTemp = Math.min(0, calculatedWindTemperature);

        int rayCount = Math.max(1, ThermMod.config.windRayCount);
        int unblockedRays = rayCount;
        Random random = new Random();
        double turbulence = playerState.windTurbulence * Math.PI / 180;
        for (int i = 0; i < rayCount; i++) {
            Vec3d direction = new Vec3d(
                    Math.cos(serverState.windPitch + random.nextDouble(-turbulence, turbulence))
                            * Math.cos(serverState.windYaw + random.nextDouble(-turbulence, turbulence)),
                    Math.sin(serverState.windPitch + random.nextDouble(-turbulence, turbulence))
                            * Math.cos(serverState.windYaw + random.nextDouble(-turbulence, turbulence)),
                    Math.sin(serverState.windYaw + random.nextDouble(-turbulence, turbulence))
            );
            Vec3d start = new Vec3d(player.getX(), player.getY() + 1, player.getZ());
            BlockHitResult result = player.getWorld().raycast(new RaycastContext(
                    start,
                    start.add(direction.multiply(ThermMod.config.windRayLength)),
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.WATER,
                    player
            ));
            if (!player.getWorld().getBlockState(result.getBlockPos()).isAir()) {
                unblockedRays--;
            }
        }
        playerState.windTemp = playerState.baseWindTemp * ((double) unblockedRays / rayCount);
    }
}
