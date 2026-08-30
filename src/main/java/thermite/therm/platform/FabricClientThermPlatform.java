package thermite.therm.platform;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import thermite.therm.client.TemperatureHudOverlay;
import thermite.therm.networking.ThermNetworkingPackets;

/** Client-only registrations kept out of the dedicated-server adapter. */
public final class FabricClientThermPlatform implements ThermPlatform.Client {
    @Override
    public void registerHud() {
        HudRenderCallback.EVENT.register(new TemperatureHudOverlay());
    }

    @Override
    public void registerNetworking() {
        ThermNetworkingPackets.registerS2CPackets();
    }
}
