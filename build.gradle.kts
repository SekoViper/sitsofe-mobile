plugins {
    id("com.android.application") version "8.13.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false

    id("com.google.dagger.hilt.android") version "2.52" apply false

    // KSP version must match Kotlin; 2.0.0-1.0.24 is safe and widely mirrored
    id("com.google.devtools.ksp") version "2.0.0-1.0.24" apply false
}
