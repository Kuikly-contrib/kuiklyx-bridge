plugins {
    alias(libs.plugins.androidLibrary)
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("maven-publish")
}

repositories {
    google()
    mavenCentral()
    maven { url = uri(System.getenv("HOME") + "/maven_repo") }
    maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-public/") }
    maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/") }
}

// publish config
val kotlinVersion: String =
    project.findProperty("kotlinVersion") as? String ?: Version.DEFAULT_KOTLIN_VERSION
println("> [build] Kotlin Version: $kotlinVersion")
// publish version
val publishInfo = Publishment.loadProperties(
    project, file("publish.properties"), File(rootDir, "version.properties")
)
group = publishInfo.groupId
version = Version.appendSubVersion(publishInfo.version, kotlinVersion)

// dependency version
val kuiklyBase = project.findProperty("kuiklyCoreVersion") as String
// 鸿蒙构建：Kuikly Core 使用 ohos 后缀版本，groupId 使用 kuikly-open
val kuiklyOhosVersion = Version.appendSubVersion(kuiklyBase, "2.0.21-ohos")
println("> [build] Kuikly Version: $kuiklyOhosVersion")

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
        publishLibraryVariants("release")
    }

    listOf(iosX64(), iosArm64(), iosSimulatorArm64())
    js(IR) {
        browser()
    }

    ohosArm64 {
        binaries {
            sharedLib()
        }
    }

    cocoapods {
        name = "SharedBridge"
        summary = "Kuikly plugin router base bridge module."
        homepage = ""
        version = "1.0"
        ios.deploymentTarget = "16.0"
        framework {
            baseName = "SharedBridge"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.tencent.kuikly-open:core:${kuiklyOhosVersion}")
            }
        }
//        commonTest.dependencies {
//            implementation(libs.kotlin.test)
//        }
    }
}

android {
    namespace = "com.tencent.kuiklyx.bridge"
    compileSdk = 35
    defaultConfig {
        minSdk = 23
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set(publishInfo.pomInfo?.name)
            description.set(publishInfo.pomInfo?.description)
            url.set(publishInfo.pomInfo?.url)

            developers {
                developer {
                    email.set(publishInfo.pomInfo?.email)
                }
            }
        }
    }

    repositories {
        maven {
            // standard
            url = publishInfo.createRepoURI()
            if (!publishInfo.isLocalRepo) {
                credentials {
                    username = publishInfo.username
                    password = publishInfo.token
                }
            }
        }
    }
}