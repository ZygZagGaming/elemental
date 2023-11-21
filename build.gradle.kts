plugins {
    kotlin("multiplatform") version "1.9.0"
}

group = "com.zygzag"
version = "2.0.0"

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven") }
}

dependencies {

}

kotlin {
    js(IR) {
        binaries.executable()
        browser {
            commonWebpackConfig(Action {
                cssSupport {
                    enabled.set(true)
                }
                output?.libraryTarget = "umd"
            } )
        }

        sourceSets {
            val jsTest by getting {
                dependencies {
                    implementation(kotlin("test"))
                }
            }
            val jsMain by getting {
                dependencies {
                    val kotlinxHtmlVersion = "0.9.0"
                    implementation("org.jetbrains.kotlinx:kotlinx-html-js:$kotlinxHtmlVersion")
                }
            }
        }
    }
}