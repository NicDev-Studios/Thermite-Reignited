/*
 * Copyright (c) 2023 sparkierkan7
 * Modifications Copyright (c) 2026 NicDev-Studios
 * SPDX-License-Identifier: MIT
 */

package thermite.therm;

import me.lortseam.completeconfig.data.ConfigOptions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroups;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermite.therm.block.ThermBlocks;
import thermite.therm.block.entity.FireplaceBlockEntity;
import thermite.therm.effect.ThermStatusEffects;
import thermite.therm.item.*;
import thermite.therm.networking.ThermNetworkingPackets;
import thermite.therm.platform.FabricThermPlatform;
import thermite.therm.platform.ThermPlatform;
import thermite.therm.recipe.LeatherArmorWoolRecipe;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

//TODO: Add default values to persistent states.
//TODO: PURIFY main temperature ticking.
//TODO: Biome climate overrider in config.

public class ThermMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("therm");
	public static final String modid = "therm";
	public static final String modVersion = "6.0.0-alpha.1";
	public static final ThermPlatform PLATFORM = new FabricThermPlatform();

	//items
	public static final GoldSweetBerriesItem GOLD_SWEET_BERRIES_ITEM = new GoldSweetBerriesItem(new FabricItemSettings().maxCount(64));
	public static final IceJuiceItem ICE_JUICE_ITEM = new IceJuiceItem(new FabricItemSettings().maxCount(16));
	public static final ThermometerItem THERMOMETER_ITEM = new ThermometerItem(new FabricItemSettings().maxCount(1));
	public static final WoolClothItem WOOL_CLOTH_ITEM = new WoolClothItem(new FabricItemSettings().maxCount(64));
	public static final TesterItem TESTER_ITEM = new TesterItem(new FabricItemSettings().maxCount(1));

	//block items
	public static final BlockItem ICE_BOX_EMPTY_ITEM = new BlockItem(ThermBlocks.ICE_BOX_EMPTY_BLOCK, new FabricItemSettings());
	public static final BlockItem ICE_BOX_FREEZING_ITEM = new BlockItem(ThermBlocks.ICE_BOX_FREEZING_BLOCK, new FabricItemSettings());
	public static final BlockItem ICE_BOX_FROZEN_ITEM = new BlockItem(ThermBlocks.ICE_BOX_FROZEN_BLOCK, new FabricItemSettings());
	public static final BlockItem FIREPLACE_ITEM = new BlockItem(ThermBlocks.FIREPLACE_BLOCK, new FabricItemSettings());

	//block entities
	//public static final BlockEntityType<FireplaceBlockEntity> FIREPLACE_BLOCK_ENTITY = null;
	public static final BlockEntityType<FireplaceBlockEntity> FIREPLACE_BLOCK_ENTITY = Registry.register(
			Registries.BLOCK_ENTITY_TYPE,
			new Identifier(modid, "fireplace_block_entity"),
			FabricBlockEntityTypeBuilder.create(FireplaceBlockEntity::new, ThermBlocks.FIREPLACE_BLOCK).build()
	);

	//special recipes
	public static final RecipeSerializer<LeatherArmorWoolRecipe> LEATHER_ARMOR_WOOL_RECIPE_SERIALIZER = PLATFORM.createLeatherArmorRecipeSerializer();

	//config
	public static final ThermConfig config = new ThermConfig();

	@Override
	public void onInitialize() {

		config.load();
		ConfigOptions.mod(modid).branch(new String[]{"branch", "config"});

		PLATFORM.registerContent();

		PLATFORM.registerNetworking();

		//events
		EventListeners.register();

		//commands
		Commands.register();

	}

	public static Identifier id(String path) {
		return new Identifier(modid, path);
	}
}
