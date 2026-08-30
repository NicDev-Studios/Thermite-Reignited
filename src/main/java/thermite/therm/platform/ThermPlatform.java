package thermite.therm.platform;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.recipe.RecipeSerializer;
import thermite.therm.ServerState;
import thermite.therm.ThermPlayerState;
import thermite.therm.core.EnvironmentSnapshot;
import thermite.therm.core.TemperatureState;
import thermite.therm.recipe.LeatherArmorWoolRecipe;

/**
 * Internal boundary for Minecraft/Fabric APIs that vary between adapters.
 * Version-specific registration, player/world access, persistence and packet
 * details stay on this side of the shared temperature engine.
 */
public interface ThermPlatform {
    void registerContent();

    void registerNetworking();

    RecipeSerializer<LeatherArmorWoolRecipe> createLeatherArmorRecipeSerializer();

    ServerState getServerState(MinecraftServer server);

    ThermPlayerState getPlayerState(ServerPlayerEntity player);

    EnvironmentSnapshot captureEnvironment(
            MinecraftServer server,
            ServerPlayerEntity player,
            ServerState serverState,
            ThermPlayerState playerState
    );

    void applyDamage(ServerPlayerEntity player, TemperatureState state);

    void synchronize(ServerPlayerEntity player, ServerState serverState, TemperatureState state);

    /** Client-only half of the adapter; never loaded by the dedicated-server entrypoint. */
    interface Client {
        void registerHud();

        void registerNetworking();
    }
}
