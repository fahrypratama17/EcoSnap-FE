import com.android.build.api.variant.BuildConfigField
import java.net.URI
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.ecosnap"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.ecosnap"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// Backend origin, e.g. http://10.0.2.2:8080/ . Source order: -PAPI_BASE_URL, then local.properties.
// Only a valid origin is accepted (no credentials, path, query or fragment); output always ends with "/".
object BackendUrl {
    fun normalizeOrigin(raw: String, requireHttps: Boolean): String {
        val uri = try {
            URI(raw.trim())
        } catch (_: Exception) {
            throw GradleException("API_BASE_URL is not a valid URL.")
        }
        val scheme = uri.scheme?.lowercase()
        if (scheme != "http" && scheme != "https") {
            throw GradleException("API_BASE_URL must start with http:// or https://.")
        }
        if (requireHttps && scheme != "https") {
            throw GradleException("Release builds require an https:// API_BASE_URL.")
        }
        if (uri.host.isNullOrEmpty()) {
            throw GradleException("API_BASE_URL must contain a host.")
        }
        if (uri.port != -1 && uri.port !in 1..65535) {
            throw GradleException("API_BASE_URL port must be between 1 and 65535.")
        }
        if (uri.rawUserInfo != null || uri.rawQuery != null || uri.rawFragment != null ||
            !(uri.rawPath.isNullOrEmpty() || uri.rawPath == "/")
        ) {
            throw GradleException(
                "API_BASE_URL must be an origin only (no credentials, path, query or fragment)."
            )
        }
        val port = if (uri.port == -1) "" else ":${uri.port}"
        return "$scheme://${uri.host}$port/"
    }
}

val apiBaseUrlSetting: Provider<String> = providers.gradleProperty("API_BASE_URL").orElse(
    providers.fileContents(rootProject.layout.projectDirectory.file("local.properties"))
        .asText
        .map { text -> Properties().apply { load(text.reader()) }.getProperty("API_BASE_URL").orEmpty() }
        .map { it.trim() }
        .filter { it.isNotEmpty() }
)

val debugApiBaseUrl: Provider<String> = apiBaseUrlSetting
    .orElse("http://10.0.2.2:8080/")
    .map { BackendUrl.normalizeOrigin(it, requireHttps = false) }

// Evaluated only when a release variant actually needs it (BuildConfig generation / compile).
val releaseApiBaseUrl: Provider<String> = apiBaseUrlSetting
    .orElse("")
    .map {
        if (it.isBlank()) {
            throw GradleException(
                "Release builds require an explicit HTTPS API_BASE_URL " +
                    "(-PAPI_BASE_URL=https://... or local.properties)."
            )
        }
        BackendUrl.normalizeOrigin(it, requireHttps = true)
    }

// Generates res/xml/network_security_config.xml for debug: cleartext only for the configured backend host.
abstract class GenerateDebugNetworkSecurityConfig : DefaultTask() {
    @get:Input
    abstract val baseUrl: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val uri = URI(baseUrl.get())
        val domainConfig = if (uri.scheme == "http") {
            val host = uri.host.removePrefix("[").removeSuffix("]")
            """
            |    <domain-config cleartextTrafficPermitted="true">
            |        <domain includeSubdomains="false">$host</domain>
            |    </domain-config>
            |""".trimMargin()
        } else {
            ""
        }
        val file = outputDir.get().file("xml/network_security_config.xml").asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            |<?xml version="1.0" encoding="utf-8"?>
            |<network-security-config>
            |    <base-config cleartextTrafficPermitted="false" />
            |$domainConfig</network-security-config>
            |""".trimMargin()
        )
    }
}

androidComponents {
    onVariants { variant ->
        val isRelease = variant.buildType == "release"
        val url = if (isRelease) releaseApiBaseUrl else debugApiBaseUrl
        variant.buildConfigFields?.put(
            "API_BASE_URL",
            url.map { BuildConfigField("String", "\"$it\"", "Backend base URL (origin ending with /)") }
        )
        if (!isRelease) {
            val generate = tasks.register<GenerateDebugNetworkSecurityConfig>(
                "generate${variant.name.replaceFirstChar { it.uppercase() }}NetworkSecurityConfig"
            ) {
                baseUrl.set(debugApiBaseUrl)
                outputDir.set(layout.buildDirectory.dir("generated/res/backendNetworkSecurity/${variant.name}"))
            }
            variant.sources.res?.addGeneratedSourceDirectory(generate, GenerateDebugNetworkSecurityConfig::outputDir)
        }
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.icons.lucide.cmp)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.android.sdk)
    implementation(libs.okhttp)
    implementation(libs.gson.v2132)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.coil.compose)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}