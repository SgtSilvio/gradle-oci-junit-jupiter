# Integration of [gradle-oci](https://github.com/sgtsilvio/gradle-oci) and [junit-jupiter](https://github.com/junit-team/junit5/)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.sgtsilvio/gradle-oci-junit-jupiter/badge.svg?style=for-the-badge)](https://central.sonatype.com/artifact/io.github.sgtsilvio/gradle-oci-junit-jupiter)
[![javadoc](https://javadoc.io/badge2/io.github.sgtsilvio/gradle-oci-junit-jupiter/javadoc.svg?style=for-the-badge)](https://javadoc.io/doc/io.github.sgtsilvio/gradle-oci-junit-jupiter)
[![GitHub](https://img.shields.io/github/license/sgtsilvio/gradle-oci-junit-jupiter?color=brightgreen&style=for-the-badge)](LICENSE)
[![GitHub Workflow Status (with branch)](https://img.shields.io/github/actions/workflow/status/sgtsilvio/gradle-oci-junit-jupiter/check.yml?branch=main&style=for-the-badge)](https://github.com/SgtSilvio/gradle-oci-junit-jupiter/actions/workflows/check.yml?query=branch%3Amain)

## How to Use

Use the Gradle OCI Plugin and add the dependency on `gradle-oci-junit-jupiter` in your `build.gradle(.kts)`:

```kotlin
plugins {
    java
    id("io.github.sgtsilvio.gradle.oci") version "0.24.0"
}

repositories {
    mavenCentral()
}

oci {
    registries {
        dockerHub {
            optionalCredentials()
        }
    }
}

testing {
    suites {
        "test"(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation("io.github.sgtsilvio:gradle-oci-junit-jupiter:0.7.0")
            }
            oci.of(this) {
                imageDependencies {
                    runtime("your:image:123")
                }
            }
        }
    }
}
```

### Configure Docker on MacOS / Windows

On MacOS and Windows `host.docker.internal` needs to be configured as an insecure registry in the Docker daemon configuration.
Usually, `host.docker.internal` resolves to the address `192.168.65.254`.

```json
{
  "insecure-registries": [
    "192.168.65.254/32"
  ]
}
```

Your Docker subnet may be configured differently.
You can determine the IP address by resolving the domain name `host.docker.internal` inside a container using this command:

```shell
docker run --rm busybox nslookup host.docker.internal
```

### Start a TestContainer in Your JUnit Test

Example in Java:

```java
class ContainerTest {
    @Test
    void start() {
        try (final GenericContainer<?> container = new GenericContainer<>(OciImages.getImageName("your/image:123"))) {
            container.start();
            //...
        }
    }
}
```
