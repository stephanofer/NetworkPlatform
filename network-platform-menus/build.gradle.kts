val paperApiVersion = providers.gradleProperty("paperApiVersion").get()

dependencies {
    api(project(":network-platform-paper"))
    compileOnly("io.papermc.paper:paper-api:$paperApiVersion")
}
