plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.defaults)
    alias(libs.plugins.metadata)
    `maven-publish`
    signing
    alias(libs.plugins.nexusPublish)
}

group = "io.github.sgtsilvio"
description = "Integration of gradle-oci and junit-jupiter"

metadata {
    readableName = "Integration of gradle-oci and junit-jupiter"
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
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
}

dependencies {
    api(libs.testcontainers)
    implementation(libs.junit.platform.launcher)
    implementation(libs.oci.registry)
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["maven"])
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl = uri("https://s01.oss.sonatype.org/service/local/")
            snapshotRepositoryUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        }
    }
}
