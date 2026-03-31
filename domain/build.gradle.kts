plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:featureflags"))
    implementation(project(":core:model"))
    implementation(project(":core:runtime-config"))
    implementation(project(":core:telemetry"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)
}
