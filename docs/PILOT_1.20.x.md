# Thermite 1.20.x pilot

## Build matrix

| Node | Minecraft runtime | Yarn | Fabric Loader | Fabric API | CompleteConfig | Artifact suffix |
| --- | --- | --- | --- | --- | --- | --- |
| `1.20.1` | 1.20.1 | `1.20.1+build.3` | 0.14.21 | 0.83.1+1.20.1 | 2.5.0 | `mc1.20.1` |
| `1.20.4` | 1.20.3–1.20.4 | `1.20.4+build.3` | 0.15.11 | 0.97.3+1.20.4 | 2.5.3 | `mc1.20.3-1.20.4` |

The build uses Stonecutter 0.9.7, Loom 1.17.12 through the Loom back-compat
plugin, and Gradle 9.5.1. Archives have stable entry order and timestamps.

## Architecture

- `thermite.therm.core` contains the Minecraft-independent temperature engine,
  state, environment snapshot, and config model.
- `ThermConfigAdapter` is the only bridge from CompleteConfig to the core model.
- `ThermPlatform` owns content/recipe/network/HUD registration, biome and player
  access, persistent state access, damage, and S2C synchronization.
- `FabricThermPlatform` and `FabricClientThermPlatform` implement that boundary
  for the current 1.20.x nodes.
- `TemperatureService` runs once per second from the server tick. No C2S packet
  can request or accelerate a temperature sample.

The S2C payload order remains `temperature`, `direction`, `windPitch`,
`windYaw`, and `windTemperature`. Existing registry identifiers and persistence
keys are unchanged, including player temperature fields, fireplace `time`, and
item NBT key `wool`. CompleteConfig continues to use `config/therm.conf`.

## Version adapters

Stonecutter conditionals are intentionally limited to the API breaks introduced
for 1.20.3:

- block codec and block-entity ticker validation in `FireplaceBlock`;
- special crafting recipe constructor/output changes;
- `PersistentState.Type` loading.

Pack metadata is expanded per node. The 1.20.4 resource pack advertises formats
18–22 so the same artifact can be tested on 1.20.3 and 1.20.4.

## Automated verification

```powershell
.\gradlew.bat client
.\gradlew.bat client1201
.\gradlew.bat server
.\gradlew.bat server1201
.\gradlew.bat testAll
.\gradlew.bat jars
.\gradlew.bat pilotCheck
.\gradlew.bat ideaRuns
.\gradlew.bat thermiteHelp
```

The server commands intentionally stop at Mojang's EULA on a new checkout.
After the developer has reviewed and accepted it in each ignored run directory,
repeat the commands and stop each server with `stop` after the `Done` line.

## Manual acceptance checklist

- Start client and dedicated server on 1.20.1, 1.20.3, and 1.20.4.
- Copy a disposable 1.20.1 test world, then verify player state, wool armor NBT,
  fireplace time, blocks, and items after loading it in 1.20.4.
- Exercise daytime/night, rain/snow, water, armor, held items, heating/cooling
  blocks, wind shelter/height, Ice Box, Fireplace, Cooling, and both HUD styles.
- Verify all recipes and inspect both client and server logs for missing assets,
  recipe errors, registry errors, and mixin failures.
- In multiplayer, use an unmodified client and a client that sends the removed
  legacy temperature packet identifier. Neither may alter the server sample rate.

If the 1.20.4-compiled jar fails specifically on a 1.20.3 runtime, add a `1.20.3`
Stonecutter node in this branch; do not create a separate long-lived branch.
