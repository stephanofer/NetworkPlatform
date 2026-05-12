val placeholderApiVersion = "2.12.2"

repositories {
    maven("https://repo.extendedclip.com/releases/")
}

dependencies {
    api(project(":network-platform-paper"))
    compileOnly(libs.paper.api)
    compileOnly("me.clip:placeholderapi:$placeholderApiVersion")
}
