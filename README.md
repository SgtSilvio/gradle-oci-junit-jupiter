# Integration of [gradle-oci](https://github.com/sgtsilvio/gradle-oci) and [junit-jupiter](https://github.com/junit-team/junit5/)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.sgtsilvio/gradle-oci-junit-jupiter/badge.svg?style=for-the-badge)](https://maven-badges.herokuapp.com/maven-central/io.github.sgtsilvio/gradle-oci-junit-jupiter)
[![javadoc](https://javadoc.io/badge2/io.github.sgtsilvio/gradle-oci-junit-jupiter/javadoc.svg?style=for-the-badge)](https://javadoc.io/doc/io.github.sgtsilvio/gradle-oci-junit-jupiter)
[![GitHub](https://img.shields.io/github/license/sgtsilvio/gradle-oci-junit-jupiter?color=brightgreen&style=for-the-badge)](LICENSE)

## How to Use

### Add the Dependency

Add the following to your `build.gradle(.kts)`:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    testImplementation("io.github.sgtsilvio:gradle-oci-junit-jupiter:0.2.0")
}
```

### Use gradle-oci

Example configuration in `build.gradle(.kts)`:

```kotlin
plugins {
    id("io.github.sgtsilvio.gradle.oci")
}

oci {
    imageDependencies.forTest(tasks.test) {
        add("example:example:123")
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

### Start a TestContainer in Your JUnit Test

Example in Java:

```java
class ContainerTest {
    @Test
    void testOnce() {
        final GenericContainer<?> container = new GenericContainer(OciImages.getImageName("example/example:123"));
        container.start();
        ...
    }
}
```
