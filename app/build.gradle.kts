plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.legacy.kapt)
    alias(libs.plugins.hilt.android)
}

fun escapedBuildConfigString(value: String): String {
    return "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""
}

val dotChatSupabaseUrl = providers.gradleProperty("DOTCHAT_SUPABASE_URL")
    .orElse(providers.environmentVariable("DOTCHAT_SUPABASE_URL"))
    .orNull
    .orEmpty()

val dotChatSupabaseAnonKey = providers.gradleProperty("DOTCHAT_SUPABASE_ANON_KEY")
    .orElse(providers.environmentVariable("DOTCHAT_SUPABASE_ANON_KEY"))
    .orNull
    .orEmpty()

val dotChatLiveKitUrl = providers.gradleProperty("DOTCHAT_LIVEKIT_URL")
    .orElse(providers.environmentVariable("DOTCHAT_LIVEKIT_URL"))
    .orNull
    .orEmpty()

val dotChatLiveKitTokenEndpoint = providers.gradleProperty("DOTCHAT_LIVEKIT_TOKEN_ENDPOINT")
    .orElse(providers.environmentVariable("DOTCHAT_LIVEKIT_TOKEN_ENDPOINT"))
    .orNull
    .orEmpty()

android {
    namespace = "com.streamgram.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.dotchat"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.2.0-alpha"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "SUPABASE_URL", escapedBuildConfigString(dotChatSupabaseUrl))
        buildConfigField("String", "SUPABASE_ANON_KEY", escapedBuildConfigString(dotChatSupabaseAnonKey))
        buildConfigField("String", "LIVEKIT_URL", escapedBuildConfigString(dotChatLiveKitUrl))
        buildConfigField(
            "String",
            "LIVEKIT_TOKEN_ENDPOINT",
            escapedBuildConfigString(dotChatLiveKitTokenEndpoint),
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":core:contacts"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:featureflags"))
    implementation(project(":core:i18n"))
    implementation(project(":core:livekit"))
    implementation(project(":core:model"))
    implementation(project(":core:network"))
    implementation(project(":core:navigation"))
    implementation(project(":core:runtime-config"))
    implementation(project(":core:security"))
    implementation(project(":core:supabase"))
    implementation(project(":core:telemetry"))
    implementation(project(":domain"))
    implementation(project(":feature:onboarding"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:comments"))
    implementation(project(":feature:channels"))
    implementation(project(":feature:chatlist"))
    implementation(project(":feature:chat"))
    implementation(project(":feature:contacts"))
    implementation(project(":feature:calls"))
    implementation(project(":feature:notifications"))
    implementation(project(":feature:profile"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:privacy"))
    implementation(project(":feature:sessions"))
    implementation(project(":feature:create-chat"))
    implementation(project(":feature:create-channel"))
    implementation(project(":feature:language"))
    implementation(project(":data"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.hilt.android)
    implementation(libs.bundles.compose)

    debugImplementation(libs.androidx.compose.ui.tooling)
    kapt(libs.hilt.compiler)
}

kapt {
    correctErrorTypes = true
}
