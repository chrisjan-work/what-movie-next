plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinter)
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
        // TODO: proper url
        buildConfigField("String", "BASE_URL", "\"localhost:8080\"")
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
}

buildscript {
    dependencies {
        // kotlinter rules for compose
        classpath(libs.rules.ktlint)
    }
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
