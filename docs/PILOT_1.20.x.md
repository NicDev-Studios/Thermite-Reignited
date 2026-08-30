<!--
Copyright (c) 2023 sparkierkan7
Modifications Copyright (c) 2026 NicDev-Studios
SPDX-License-Identifier: MIT
-->

# Thermite 1.20.x

This is a quick note about the current 1.20.x setup. The important part is
that the old code and the new multi-version build are kept separate.

## Branches

`legacy` is the old `master` branch. It has its own old Gradle build and is
kept as-is for reference. New Minecraft version work does not go there.

`1.20.x` is the active branch for Minecraft 1.20. The shared mod code lives in
`src/main` and is written only once. Stonecutter creates the version projects
and applies the few API fixes that are needed. We do not copy the whole mod
into a separate folder for every version.

## Supported versions

| Minecraft | Notes | JAR |
| --- | --- | --- |
| 1.20.1 | Reference build for comparison | `therm-6.0.0-alpha.1+mc1.20.1.jar` |
| 1.20.2 | Separate build and adapter | `therm-6.0.0-alpha.1+mc1.20.2.jar` |
| 1.20.3 | Uses the 1.20.4 adapter | `therm-6.0.0-alpha.1+mc1.20.3-1.20.4.jar` |
| 1.20.4 | Build node for the newer 1.20 API | `therm-6.0.0-alpha.1+mc1.20.3-1.20.4.jar` |

So the current 1.20.x release range is **1.20.2 through 1.20.4**. The 1.20.1
build stays around so changes can still be compared against the old baseline.

## What is version-specific?

Only the parts that actually changed in Minecraft are adapted:

- block codecs and block-entity tickers;
- special crafting recipes;
- the `PersistentState` loading API.

Temperature calculation, player data, NBT keys, recipes and resources are
shared. Temperature ticks are calculated on the server, so a client cannot
request extra ticks or speed up the calculation.

## Build and run

Run these from the project directory:

```powershell
.\gradlew.bat pilotCheck       # run tests and build all JARs
.\gradlew.bat testAll         # run tests for every version
.\gradlew.bat jars            # build all versioned JARs
.\gradlew.bat client1202      # start the 1.20.2 client
.\gradlew.bat client1204      # start the 1.20.4 client
.\gradlew.bat server1202      # start a 1.20.2 server
.\gradlew.bat server1204      # start a 1.20.4 server
```

The 1.20.x workflow runs on every push and pull request on Linux and Windows.
`pilotCheck` compiles all three build nodes, runs the tests and collects the
three JARs. It uses Gradle 9.7.1 and Java 21 to run the build; the mod itself
is still compiled for Java 17.

There are currently 12 tests per build node: temperature behaviour, NBT
persistence, and the important packaged resources/metadata. That checks the
shared code and catches version-specific compile problems. It does not replace
starting the game and loading a test world.

## Still worth checking in-game

- Start the client and dedicated server on 1.20.2, 1.20.3 and 1.20.4.
- Load a copied 1.20.1 world and check player data, wool armour, fireplace
  time, blocks and items.
- Try temperature changes from weather, night, water, armour, wind, the Ice
  Box, Fireplace, Cooling and all recipes/HUDs.
- Check client and server logs for missing resources, recipe errors or mixin
  failures.
