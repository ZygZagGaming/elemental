plugins {
    kotlin("js") version "1.8.20"
}

group = "com.zygzag"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    js(IR) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
                output?.libraryTarget = "umd"
            }
        }
    }
}