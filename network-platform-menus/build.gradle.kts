val paperApiVersion = providers.gradleProperty("paperApiVersion").get()
val zMenuApiVersion = "1.1.1.3"
val foliaLibVersion = "0.5.1"

repositories {
    maven("https://repo.groupez.dev/releases")
    maven("https://repo.tcoded.com/releases")
}

dependencies {
    api(project(":network-platform-paper"))
    compileOnly("io.papermc.paper:paper-api:$paperApiVersion")
    compileOnly("fr.maxlego08.menu:zmenu-api:$zMenuApiVersion")

    testImplementation("io.papermc.paper:paper-api:$paperApiVersion")
    testImplementation("fr.maxlego08.menu:zmenu-api:$zMenuApiVersion")
    testImplementation("com.tcoded:FoliaLib:$foliaLibVersion")
    testImplementation("org.mockito:mockito-core:5.18.0")
}

tasks.withType<Test>().configureEach {
    dependsOn(":network-platform-paper:shadowJar")
}
