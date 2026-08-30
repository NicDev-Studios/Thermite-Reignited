plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "1.20.1"

val supportedMinecraftVersions = setOf("1.20.1", "1.20.4")
val selectedMinecraftVersion = providers.gradleProperty("mc").orElse("1.20.4")

fun selectedNodeTask(taskName: String) = selectedMinecraftVersion.map { version ->
    require(version in supportedMinecraftVersions) {
        "Unsupported -Pmc=$version. Supported versions: ${supportedMinecraftVersions.joinToString()}"
    }
    ":$version:$taskName"
}

tasks.register("client") {
    group = "thermite"
    description = "Starts the default Minecraft 1.20.4 development client."
    dependsOn(selectedNodeTask("runClient"))
}

tasks.register("client1201") {
    group = "thermite"
    description = "Starts the Minecraft 1.20.1 development client."
    dependsOn(":1.20.1:runClient")
}

tasks.register("client1204") {
    group = "thermite"
    description = "Starts the Minecraft 1.20.4 development client."
    dependsOn(":1.20.4:runClient")
}

tasks.register("server") {
    group = "thermite"
    description = "Starts the default Minecraft 1.20.4 development server."
    dependsOn(selectedNodeTask("runServer"))
}

tasks.register("server1201") {
    group = "thermite"
    description = "Starts the Minecraft 1.20.1 development server."
    dependsOn(":1.20.1:runServer")
}

tasks.register("server1204") {
    group = "thermite"
    description = "Starts the Minecraft 1.20.4 development server."
    dependsOn(":1.20.4:runServer")
}

tasks.register("jars") {
    group = "thermite"
    description = "Builds and collects every supported Thermite jar."
    dependsOn(":1.20.1:buildAndCollect", ":1.20.4:buildAndCollect")
}

tasks.register("testAll") {
    group = "thermite"
    description = "Runs the shared tests against every supported Minecraft version."
    dependsOn(":1.20.1:test", ":1.20.4:test")
}

tasks.register("pilotCheck") {
    group = "thermite"
    description = "Runs all tests and creates all pilot jars."
    dependsOn("testAll", "jars")
}

tasks.register("ideaRuns") {
    group = "thermite"
    description = "Regenerates version-labelled IntelliJ client and server configurations."
    dependsOn(":1.20.1:ideaSyncTask", ":1.20.4:ideaSyncTask")
}

tasks.register("thermiteHelp") {
    group = "thermite"
    description = "Prints the short Thermite development command reference."
    doLast {
        println(
            """
            Thermite development commands
            -----------------------------
            gradlew client                         Start Minecraft 1.20.4
            gradlew client1201                     Start Minecraft 1.20.1
            gradlew client1204                     Start Minecraft 1.20.4 explicitly
            gradlew server                         Start server 1.20.4
            gradlew server1201                     Start server 1.20.1
            gradlew server1204                     Start server 1.20.4 explicitly
            gradlew jars                           Build both distributable jars
            gradlew testAll                        Test both versions
            gradlew pilotCheck                     Test and build everything
            gradlew ideaRuns                       Regenerate IntelliJ run entries
            gradlew tasks --group thermite         Show these tasks

            Advanced: quoted version selection also works on PowerShell:
            gradlew client "-Pmc=1.20.1"
            """.trimIndent()
        )
    }
}
