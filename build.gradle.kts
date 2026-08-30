import org.gradle.api.tasks.bundling.AbstractArchiveTask

plugins {
    id("dev.kikugie.loom-back-compat")
    `maven-publish`
}

val modId = property("mod.id") as String
val modVersion = property("mod.version") as String

group = property("mod.group") as String
version = "$modVersion+mc${sc.properties.get<String>("mod.artifact_mc")}"
base.archivesName = modId

repositories {
    maven("https://jitpack.io")
    maven("https://maven.terraformersmc.com/")
    maven("https://maven.shedaniel.me/")
    maven("https://maven.siphalor.de/")
    maven("https://maven.isxander.dev/releases")
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    mappings("net.fabricmc:yarn:${sc.properties.get<String>("deps.yarn")}:v2")

    modImplementation("net.fabricmc:fabric-loader:${sc.properties.get<String>("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${sc.properties.get<String>("deps.fabric_api")}")
    modImplementation("com.github.Lortseam.completeconfig:base:${sc.properties.get<String>("deps.completeconfig")}")

    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json")
    runConfigs.all {
        preferGradleTask = true
        runDirectory = rootProject.file("run/${sc.current.project}")
    }
}

tasks {
    withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }

    processResources {
        val props = mapOf(
            "version" to project.version,
            "minecraft" to sc.properties.get<String>("mod.mc_compat"),
            "loader" to sc.properties.get<String>("mod.loader_compat"),
            "completeconfig" to sc.properties.get<String>("mod.completeconfig_compat"),
            "pack_format" to sc.properties.get<String>("mod.pack_format"),
            "pack_format_min" to sc.properties.get<String>("mod.pack_format_min"),
            "pack_format_max" to sc.properties.get<String>("mod.pack_format_max")
        )
        inputs.properties(props)
        filesMatching(listOf("fabric.mod.json", "pack.mcmeta")) {
            expand(props)
        }
    }

    withType<JavaCompile>().configureEach {
        options.release = 17
        options.encoding = "UTF-8"
    }

    test {
        useJUnitPlatform()
    }

    jar {
        from(rootProject.file("LICENSE")) {
            rename("LICENSE", "LICENSE_therm")
        }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds the selected Thermite variant and collects its distributable jar."
        dependsOn(loomx.modJar)
        from(loomx.modJar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.dir("libs/$modVersion"))
    }
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
