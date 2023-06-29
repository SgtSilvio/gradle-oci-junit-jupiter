# Integration of gradle-oci and junit-jupiter

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
    testImplementation("io.github.sgtsilvio:gradle-oci-junit-jupiter:0.1.0")
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
        default("example/example:123")
    }
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
