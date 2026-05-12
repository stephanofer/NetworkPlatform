val zMenuApiVersion = "1.1.1.3"
val foliaLibVersion = "0.5.1"

repositories {
    maven("https://repo.groupez.dev/releases")
    maven("https://repo.tcoded.com/releases")
}

dependencies {
    api(project(":network-platform-paper"))
    compileOnly(libs.paper.api)
    compileOnly("fr.maxlego08.menu:zmenu-api:$zMenuApiVersion")

    testImplementation(libs.paper.api)
    testImplementation("fr.maxlego08.menu:zmenu-api:$zMenuApiVersion")
    testImplementation("com.tcoded:FoliaLib:$foliaLibVersion")
    testImplementation(libs.mockito.core)
}

tasks.withType<Test>().configureEach {
    dependsOn(":network-platform-paper:shadowJar")
}
