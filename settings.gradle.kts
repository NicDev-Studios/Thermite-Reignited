// Copyright (c) 2023 sparkierkan7
// Modifications Copyright (c) 2026 NicDev-Studios
// SPDX-License-Identifier: MIT

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.kikugie.dev/snapshots")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.7"
    id("dev.kikugie.loom-back-compat") version "0.4.2"
}

stonecutter {
    create(rootProject) {
        versions("1.20.1", "1.20.2", "1.20.4")
        vcsVersion = "1.20.1"
    }
}

rootProject.name = "Thermite-Reignited"
