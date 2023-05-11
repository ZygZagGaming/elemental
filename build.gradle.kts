plugins {
    kotlin("js") version "1.7.10"
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
    js(LEGACY) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
            webpackTask {
                output.libraryTarget = "commonjs2"

            }
        }
    }
}