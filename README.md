<!--
Copyright (c) 2023 sparkierkan7
Modifications Copyright (c) 2026 NicDev-Studios
SPDX-License-Identifier: MIT
-->

# Thermite-Reignited

[![1.20.x CI](https://github.com/NicDev-Studios/Thermite-Reignited/actions/workflows/build.yml/badge.svg?branch=1.20.x)](https://github.com/NicDev-Studios/Thermite-Reignited/actions/workflows/build.yml?query=branch%3A1.20.x)
[![legacy CI](https://github.com/NicDev-Studios/Thermite-Reignited/actions/workflows/build.yml/badge.svg?branch=legacy)](https://github.com/NicDev-Studios/Thermite-Reignited/actions/workflows/build.yml?query=branch%3Alegacy)
[![Minecraft 1.20.2-1.20.4](https://img.shields.io/badge/Minecraft-1.20.2--1.20.4-62b47a)](docs/PILOT_1.20.x.md)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## Branches

- `1.20.x` ist die aktuelle Entwicklungs-Branch. Sie unterstützt Minecraft
  1.20.2, 1.20.3 und 1.20.4.
- `legacy` ist der alte Stand aus `master` mit seinem eigenen Build. Neue
  Versionsadapter gehören nicht in diese Branch.

Der gemeinsame Code wird nur einmal gepflegt. Stonecutter baut daraus je
Minecraft-Version ein eigenes kompatibles JAR.

## Development quick start

The root project exposes short commands, so the Stonecutter project paths do not
need to be memorized:

```powershell
.\gradlew.bat client                    # Start Minecraft 1.20.4
.\gradlew.bat client1201                # Start Minecraft 1.20.1
.\gradlew.bat client1202                # Start Minecraft 1.20.2
.\gradlew.bat client1204                # Start Minecraft 1.20.4 explicitly
.\gradlew.bat server1202                # Start server 1.20.2
.\gradlew.bat server1204                # Start server 1.20.4
.\gradlew.bat testAll                   # Run tests for every version
.\gradlew.bat jars                      # Build every versioned JAR
.\gradlew.bat pilotCheck                # Test and build every pilot variant
.\gradlew.bat ideaRuns                  # Regenerate versioned IntelliJ runs
.\gradlew.bat thermiteHelp              # Show all short commands
```

The 1.20.4 client is the default because it is the newest supported node.

### License
This mod is available under the MIT license.

### Current 1.20.x support

- Minecraft: 1.20.2, 1.20.3 and 1.20.4
- Fabric API: 0.91.6+1.20.2 and 0.97.3+1.20.4
- CompleteConfig: 2.5.0 for 1.20.2 and 2.5.3 for 1.20.4
- Build runtime: Java 21 (the mod is compiled for Java 17)
- 1.20.1 remains available as a reference build

Exact mappings, loaders and artifact names are listed in
[`docs/PILOT_1.20.x.md`](docs/PILOT_1.20.x.md).

[Roughly Enough Items](https://modrinth.com/mod/rei/version/12.0.625+fabric) (Recommended)

## Description
This is a fabric mod that adds a body temperature system based on what biome you are in, the time of day, weather conditions and what blocks are around you. If you get too cold or hot you will start to take damage, but there are ways to maintain a good temperature.


In vanilla minecraft, biomes have there own built in temperature.
This mod takes that temperature and determines how cold or hot you should feel.
So it should work with any modded biome as long as its biome temperature is set appropriately.

### Heating
Heat making blocks will slowly increase your temperature if you are less than 3 blocks away.
Torches increase temperature by 3, fire also 3, lava 8 and campfires 16.
You can also increase your temperature by wearing leather armor. Boots increase by 1, leggings 2,  chestplate 3 and helmet 1.

### Cooling
Cold making blocks do the opposite of heat making blocks and cool you down but within a shorter range (2 blocks). Ice decreases by 1, packed ice decreases by 3 and blue ice decreases by 6. This is useful if you are in a hot climate since you can just put some ice under your floor and that will cool you down, or in the nether if you don't have fire resistance.

You will also become colder if it rains, snows, its nighttime or if you get in water.
Fire resistance will stop you from taking any damage from high temperature.
So if you are too hot drink fire resistance or make ice juice. (use [REI](https://modrinth.com/mod/rei/version/12.0.625+fabric) to see ice juice recipe). Ice juice is very useful in the nether before you get fire resistance.


### Ice Box Mechanics
The ice box is a new block added that can be used to get ice easier before getting silk touch. First craft an empty ice box (use [REI](https://modrinth.com/mod/rei/version/12.0.625+fabric) for recipe). Then you can right click it with a water bottle to fill it with water. For it to make ice it needs to be in a cold or frigid climate (taiga biomes and snowy biomes will be fine). When you have filled it and it is in a cold or frigid climate you just have to wait and it will freeze. Once it has frozen you can right click it and it will give you 3 ice and turn back into an empty ice box.

When it is in a frozen state it will act like packed ice and cool you down by 3 if you are near enough. But it will slowly melt and turn back into its unfrozen state in biomes than are not cold or frigid. The hotter the biome the faster it melts.

### Brick Fireplace Mechanics
The brick fireplace block, when lit, will heat you up by 16 in a large radius. You can light it by right clicking it with fuel items. Coal and charcoal fuel it for 100 seconds, coal blocks 900 seconds and sticks 10 seconds.

Putting brick walls, cobblestone walls or stone brick walls above the fireplace cause the smoke it makes to come out above the walls like a chimney.

### UI and Thermometer
The UI is a gauge on your bottom left. The black slider will slide left when you get cold and right when you get hot. The range is 0 - 100 where 50 is your preferred temperature.

If you hold a thermometer item in your offhand a display will pop up next to the gauge showing your exact temperature as a number:

<img src="https://cdn-raw.modrinth.com/data/ggmtYNuc/images/d7174e2fc4a91741f75495a9c9cf50f4167ae4d2.png"  width="50%" height="50%">




#### Direction Icon
The direction icon is the icon in the middle of the gauge. is a white ball when your temperature is not changing, a flame when you are getting warmer and a snowflake when you are getting colder. It basically shows the direction your temperature is headed.




#### Temperature staying still:
<img src="https://cdn-raw.modrinth.com/data/ggmtYNuc/images/03253f060bf747c8251438e3e28e50085e0c177e.png"  width="50%" height="50%">



#### Temperature warming up:
<img src="https://cdn-raw.modrinth.com/data/ggmtYNuc/images/2856cec8c24c1063d3adad1ac2bdab23fc1a4456.png"  width="50%" height="50%">




#### Temperature cooling down:
<img src="https://cdn-raw.modrinth.com/data/ggmtYNuc/images/fe9742ccde814a169dbff4c9a89dca2ff65e4866.png"  width="50%" height="50%">

## 1.20.x builds

The `1.20.x` branch uses Stonecutter to keep shared sources and small
version adapters in one branch. It produces separate, remapped JARs:

- `therm-6.0.0-alpha.1+mc1.20.1.jar`
- `therm-6.0.0-alpha.1+mc1.20.2.jar`
- `therm-6.0.0-alpha.1+mc1.20.3-1.20.4.jar`

Build and test all supported variants with:

```powershell
.\gradlew.bat testAll
.\gradlew.bat jars
```

Collected artifacts are written to `build/libs/6.0.0-alpha.1/`. See
`docs/PILOT_1.20.x.md` for the adapter matrix and manual acceptance checklist.




&emsp;
&emsp;
### Season System (Disabled By Default)
When enabled your temperature will be affected by the current season. Its main purpose is for mod compatibility but you can turn it on by itself. For example if you have a season mod installed then you would want your temperature to change with that mods seasons. You don't need to turn this on for [Fabric Seasons](https://modrinth.com/mod/fabric-seasons) because it already affects your temperature by changing the biome temperature.

### Config
Config file path is "config/therm.conf", if you want something added to the config create an issue on github.
