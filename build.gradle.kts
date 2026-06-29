import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.defaults)
    alias(libs.plugins.metadata)
    alias(libs.plugins.mavenCentralPublishing)
}

group = "io.github.sgtsilvio"

metadata {
    readableName = "Integration of gradle-oci and junit-jupiter"
    description = "Integration of gradle-oci and junit-jupiter"
    license {
        apache2()
    }
    developers {
        register("SgtSilvio") {
            fullName = "Silvio Giebl"
        }
    }
    github {
        org = "SgtSilvio"
        issues()
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

kotlin {
    jvmToolchain(21)
}

tasks.compileKotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_1_8
        apiVersion = KotlinVersion.KOTLIN_2_0
        languageVersion = KotlinVersion.KOTLIN_2_0
    }
}

tasks.compileJava {
    javaCompiler = javaToolchains.compilerFor {
        languageVersion = JavaLanguageVersion.of(8)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    api(libs.testcontainers)
    implementation(libs.junit.platform.launcher)
    implementation(libs.oci.registry)
    implementation(libs.slf4j.api)
}

publishing {
    publications {
        register<MavenPublication>("main") {
            from(components["java"])
        }
    }
}

signing {
    val signingKey = providers.gradleProperty("signingKey").orNull
    val signingPassword = providers.gradleProperty("signingPassword").orNull
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["main"])
}
