plugins {
    alias(libs.plugins.shadow)
}

val shade by configurations.creating

dependencies {
    compileOnly(libs.paper.api)
    testImplementation(libs.paper.api)

    compileOnly(libs.boosted.yaml)
    testImplementation(libs.boosted.yaml)

    shade(libs.boosted.yaml)
}

tasks.shadowJar {
    archiveClassifier.set("")
    configurations = listOf(shade)
    relocate("dev.dejvokep.boostedyaml", "com.stephanofer.networkplatform.paper.libs.boostedyaml")
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}
