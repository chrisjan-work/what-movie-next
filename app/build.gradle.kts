import java.io.FileInputStream
import java.util.Properties

/*
 * This file is part of What Movie Next.
 *
 * Copyright (C) 2024 Christiaan Janssen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.gradle.secrets)
}

val secretsProperties = Properties()
val secretsPropertiesFile = rootProject.file("secrets.properties")
if (secretsPropertiesFile.exists()) {
    secretsProperties.load(FileInputStream(secretsPropertiesFile))
} else {
    val secretsDefaultPropertiesFile = rootProject.file("secrets.default.properties")
    if (secretsDefaultPropertiesFile.exists()) {
        secretsProperties.load(FileInputStream(secretsDefaultPropertiesFile))
    }
}

android {
    namespace = "com.lairofpixies.whatmovienext"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.lairofpixies.whatmovienext"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.lairofpixies.whatmovienext.test.CucumberRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "CUCUMBER_TAG_EXPRESSION", extractCucumberTags())
        val oneWeek: Long = 1000 * 60 * 60 * 24 * 7
        buildConfigField("Long", "CACHE_EXPIRATION_TIME_MILLIS", "${oneWeek}L")

        val shareScheme = "whatmovienext"
        val shareHost = "movie"
        buildConfigField("String", "SHARE_SCHEME", "\"$shareScheme\"")
        buildConfigField("String", "SHARE_HOST", "\"$shareHost\"")
        manifestPlaceholders["shareScheme"] = shareScheme
        manifestPlaceholders["shareHost"] = shareHost
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    flavorDimensions += "testRunner"

    productFlavors {
        create("junit") {
            dimension = "testRunner"
            testInstrumentationRunnerArguments["cucumberUseAndroidJUnitRunner"] = "true"
        }
        create("cucumber") {
            dimension = "testRunner"
            testInstrumentationRunnerArguments["cucumberUseAndroidJUnitRunner"] = "false"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.13"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    signingConfigs {
        create("release") {
            keyAlias = secretsProperties["key.alias"] as String
            keyPassword = secretsProperties["key.password"] as String
            storeFile = file(secretsProperties["keystore.file"] as String)
            storePassword = secretsProperties["keystore.password"] as String
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // material icons
    implementation(libs.androidx.material.icons.extended)

    // mockk
    testImplementation(libs.mockk)

    // timber
    implementation(libs.timber)

    // datastore
    implementation(libs.androidx.datastore.preferences)

    // coroutines
    implementation(libs.coroutines.android)
    implementation(libs.coroutines.core)
    testImplementation(libs.kotlinx.coroutines.test)

    // hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    androidTestImplementation(libs.hilt.android)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // retrofit
    implementation(libs.retrofit)
    implementation(libs.moshi.converter)
    implementation(libs.moshi.kotlin)
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.okhttp.tls)
    androidTestImplementation(libs.mockwebserver)

    // cucumber
    androidTestImplementation(libs.cucumber.android)
    androidTestImplementation(libs.cucumber.android.hilt)

    // room
    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    testImplementation(libs.androidx.room.testing)

    // navigation
    implementation(libs.androidx.navigation.compose)

    // coil
    implementation(libs.coil.compose)

    // splash screen
    implementation(libs.androidx.core.splashscreen)
}

buildscript {
    dependencies {
        // kotlinter rules for compose
        classpath(libs.rules.ktlint)
    }
}

secrets {
    propertiesFileName = "secrets.properties"
    defaultPropertiesFileName = "secrets.default.properties"
}

fun extractCucumberTags(): String {
    // example:
    // ./gradlew connectedCucumberAndroidTest -Pcucumber.tags="MovieListFeature,ArchiveFeature"

    // Split comma-separated list, add @ to each element, convert to expression
    val tagsProperty = (project.findProperty("cucumber.tags") as? String?) ?: return "\"\""
    val tagsList = tagsProperty.split(",").map { "@${it.trim()}" }.filter { it.isNotEmpty() }
    val asExpression = tagsList.joinToString(separator = " or ")
    return "\"$asExpression\""
}

tasks.register("checkHeaders") {
    val headerFiles =
        listOf(
            listOf("kt", "kts", "java") to "header.kt",
            listOf("feature") to "header.gherkin",
            listOf("xml") to "header.xml",
        ).map { (extensions, path) ->
            extensions to file("../gpl_headers/$path").readLines()
        }

    doLast {
        val errors = mutableListOf<String>()
        headerFiles.forEach { (extensions, headerLines) ->
            extensions.forEach { extension ->
                val sourceFiles = fileTree(file("src/")) { include("**/*.$extension") }
                sourceFiles.forEach { file ->
                    if (file.readLines().take(headerLines.size) != headerLines) {
                        errors.add("Header mismatch in file: ${file.name}")
                    }
                }
            }
        }

        if (errors.isNotEmpty()) {
            errors.forEach { println(it) }
            throw GradleException("Header check failed.")
        } else {
            println("All headers are correct.")
        }
    }
}

tasks.forEach { task ->
    if (task.name.startsWith("lint")) {
        task.dependsOn("checkHeaders")
    }
}
