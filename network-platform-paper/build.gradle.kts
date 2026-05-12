plugins {
    id("com.gradleup.shadow") version "9.4.1"
}

val paperApiVersion = providers.gradleProperty("paperApiVersion").get()
val boostedYamlVersion = "1.3.7"
val shade by configurations.creating

dependencies {
    compileOnly("io.papermc.paper:paper-api:$paperApiVersion")
    testImplementation("io.papermc.paper:paper-api:$paperApiVersion")

    compileOnly("dev.dejvokep:boosted-yaml:$boostedYamlVersion")
    testImplementation("dev.dejvokep:boosted-yaml:$boostedYamlVersion")

    shade("dev.dejvokep:boosted-yaml:$boostedYamlVersion")
}

tasks.shadowJar {
    archiveClassifier.set("")
    configurations = listOf(shade)
    relocate("dev.dejvokep.boostedyaml", "com.stephanofer.networkplatform.paper.libs.boostedyaml")
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}
