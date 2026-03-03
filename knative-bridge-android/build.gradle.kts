plugins {
    alias(libs.plugins.androidLibrary)
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
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
println("> [build] Publish Version: $version")

// dependency version
val kuiklyBase = project.findProperty("kuiklyCoreVersion") as String
val kuiklyVersion = Version.appendSubVersion(kuiklyBase, kotlinVersion)
println("> [build] Kuikly Version: $kuiklyVersion")

val kuiklyRenderVersion = Version.appendKuiklyRenderVersion(kuiklyBase, kotlinVersion)
println("> [build] Kuikly Render Version: $kuiklyRenderVersion")

android {
    namespace = "com.tencent.kuiklyx.knative.bridge"
    compileSdk = 35

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    compileOnly("com.tencent.kuikly-open:core-render-android:${kuiklyRenderVersion}")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("Aar") {
                from(components["release"])

                groupId = publishInfo.groupId
                artifactId = publishInfo.artifactId

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
            maven {
                name = "cnb"
                url = publishInfo.createRepoURI("cnb")
                if (!publishInfo.isLocalRepo) {
                    credentials {
                        username = publishInfo["cnb_username"]
                        password = publishInfo["cnb_token"]
                    }
                }
            }
        }
    }
}