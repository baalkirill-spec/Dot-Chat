plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:tdlib"))
    implementation(project(":core:tdlib-chat"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)
}
