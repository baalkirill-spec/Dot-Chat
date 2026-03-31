pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "DotChat"

include(
    ":app",
    ":core:common",
    ":core:contacts",
    ":core:model",
    ":core:security",
    ":core:supabase",
    ":core:livekit",
    ":core:telemetry",
    ":core:featureflags",
    ":core:runtime-config",
    ":core:i18n",
    ":core:navigation",
    ":core:network",
    ":core:network-core",
    ":core:network-policy",
    ":core:database",
    ":core:datastore",
    ":core:designsystem",
    ":core:ui",
    ":core:player",
    ":domain",
    ":data",
    ":feature:onboarding",
    ":feature:auth",
    ":feature:comments",
    ":feature:channels",
    ":feature:chatlist",
    ":feature:chat",
    ":feature:contacts",
    ":feature:calls",
    ":feature:notifications",
    ":feature:profile",
    ":feature:settings",
    ":feature:privacy",
    ":feature:sessions",
    ":feature:create-chat",
    ":feature:create-channel",
    ":feature:language",
)
