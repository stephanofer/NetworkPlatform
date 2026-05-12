plugins {
    alias(libs.plugins.shadow)
}

val shade by configurations.creating

dependencies {
    api(project(":network-platform-paper"))
    compileOnly(libs.paper.api)

    compileOnly(libs.hikari)
    compileOnly(libs.flyway.core)
    compileOnly(libs.flyway.mysql)
    compileOnly(libs.mysql.connector)

    testImplementation(libs.hikari)
    testImplementation(libs.flyway.core)
    testImplementation(libs.flyway.mysql)
    testImplementation(libs.mysql.connector)

    shade(libs.hikari)
    shade(libs.flyway.core)
    shade(libs.flyway.mysql)
    shade(libs.mysql.connector)

    testImplementation(libs.paper.api)
    testImplementation(libs.mockito.core)
}

tasks.shadowJar {
    archiveClassifier.set("")
    configurations = listOf(shade)
    mergeServiceFiles()
    relocate("com.zaxxer.hikari", "com.stephanofer.networkplatform.database.libs.hikari")
    relocate("org.flywaydb", "com.stephanofer.networkplatform.database.libs.flyway")
    relocate("com.mysql", "com.stephanofer.networkplatform.database.libs.mysql")
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}

tasks.withType<Test>().configureEach {
    dependsOn(":network-platform-paper:shadowJar")
}
