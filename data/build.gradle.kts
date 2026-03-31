plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.legacy.kapt)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.streamgram.data"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:contacts"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:featureflags"))
    implementation(project(":core:i18n"))
    implementation(project(":core:livekit"))
    implementation(project(":core:model"))
    implementation(project(":core:network"))
    implementation(project(":core:network-core"))
    implementation(project(":core:network-policy"))
    implementation(project(":core:runtime-config"))
    implementation(project(":core:security"))
    implementation(project(":core:supabase"))
    implementation(project(":core:telemetry"))
    implementation(project(":domain"))

    implementation(libs.androidx.appcompat)
    implementation(libs.hilt.android)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.javax.inject)
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth)
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.realtime)
    implementation(libs.supabase.storage)
    implementation(libs.supabase.functions)
    kapt(libs.hilt.compiler)
}

kapt {
    correctErrorTypes = true
}
