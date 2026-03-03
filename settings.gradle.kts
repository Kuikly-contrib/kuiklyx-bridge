enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    val kotlinVersion: String by settings
    println("> [settings] Kotlin Version: $kotlinVersion")
    plugins {
        kotlin("multiplatform").version(kotlinVersion)
        kotlin("native.cocoapods").version(kotlinVersion)
    }

    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-public/") }
        maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/") }
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-public/") }
        maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/") }
    }
}

/**
 * kotlin version 是否 >= 1.9
 * - only check major + minor version
 *
 * @param version kotlin version
 * @return true is >=1.9
 */
@Suppress("FunctionName")
fun isKotlinVersion1_9(version: String): Boolean {
    val parts = version.split('.').take(2)
    val major = parts.firstOrNull()?.toIntOrNull() ?: 0
    val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
    return major >= 1 && minor >= 9
}

// kotlin version
val kotlinVersion: String by settings

// enable open-harmony os
val enableOHOS: String by settings
println("> [settings] Kotlin Version: ${kotlinVersion}, enableOHOS: $enableOHOS")

rootProject.name = "kuiklyx-bridge"
// build.gradle.kts for ohos
val buildFileOHOS = "build_ohos.gradle.kts"

include(":shared_bridge")
include(":knative-bridge-android")


// ohos kotlin, use buildFile ohos
if (enableOHOS == "true") {
    project(":shared_bridge").buildFileName = buildFileOHOS
}

println("> [settings] Build gradle  : ${project(":shared_bridge").buildFileName}")