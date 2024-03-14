package io.github.sgtsilvio.gradle.oci.junit.jupiter

import com.github.dockerjava.api.exception.NotFoundException
import com.sun.jna.Platform
import io.github.sgtsilvio.oci.registry.DistributionRegistryStorage
import io.github.sgtsilvio.oci.registry.OciRegistryHandler
import org.slf4j.LoggerFactory
import org.testcontainers.DockerClientFactory
import org.testcontainers.utility.DockerImageName
import reactor.netty.DisposableServer
import reactor.netty.http.server.HttpServer
import java.net.ServerSocket
import java.nio.file.Paths

/**
 * @author Silvio Giebl
 */
object OciImages {
    private var registry: DisposableServer? = null
    private val imageNames = mutableListOf<Pair<DockerImageName, Boolean>>()
    private val logger = LoggerFactory.getLogger(OciImages::class.java)

    @JvmStatic
    fun getImageName(imageName: String) = getImageName(imageName, false)

    @JvmStatic
    fun getImageName(imageName: String, retain: Boolean): DockerImageName {
        synchronized(OciImages) {
            val host = getRegistryHost()
            val port = (registry ?: startRegistry()).port()
            val dockerImageName = DockerImageName.parse("$host:$port/$imageName").asCompatibleSubstituteFor(imageName)
            imageNames += Pair(dockerImageName, retain)
            return dockerImageName
        }
    }

    private fun getRegistryHost() =
        if (Platform.isMac() || Platform.isWindows()) "host.docker.internal" else "localhost"

    private fun startRegistry(): DisposableServer {
        val registryDataDirectory = Paths.get(System.getProperty("io.github.sgtsilvio.gradle.oci.registry.data.dir"))
        logger.debug("Starting OCI registry with data directory {}", registryDataDirectory)
        val registry = HttpServer.create()
            .port(0)
            .handle(OciRegistryHandler(DistributionRegistryStorage(registryDataDirectory)))
            .bindNow()
        logger.debug("Started OCI registry on port {}", registry.port())
        this.registry = registry
        Runtime.getRuntime().addShutdownHook(Thread { cleanup() })
        cleanupLeftoverImages(registry.port())
        return registry
    }

    private fun cleanupLeftoverImages(currentRegistryPort: Int) {
        logger.debug("Checking for leftover images")
        val hostPrefix = getRegistryHost() + ":"
        val dockerClient = DockerClientFactory.instance().client()
        dockerClient.listImagesCmd().exec().flatMap { it.repoTags.toList() }.mapNotNull { imageName ->
            if (!imageName.startsWith(hostPrefix)) {
                return@mapNotNull null
            }
            val portStartIndex = hostPrefix.length
            val portEndIndex = imageName.indexOf('/', portStartIndex)
            if (portEndIndex == -1) {
                return@mapNotNull null
            }
            val port = try {
                imageName.substring(portStartIndex, portEndIndex).toInt()
            } catch (e: NumberFormatException) {
                return@mapNotNull null
            }
            Pair(port, imageName)
        }.groupBy({ it.first }, { it.second }).forEach { (port, imageNames) ->
            val isLeftover = (port == currentRegistryPort) || try {
                ServerSocket(port).close()
                // if binding the port succeeds, no registry is running on that port => leftover
                true
            } catch (ignored: Exception) {
                logger.debug("Not removing images {} because they might be in use by another test run", imageNames)
                // if binding the port fails, a registry from another test run might be running => not a leftover
                false
            }
            if (isLeftover) {
                for (imageName in imageNames) {
                    try {
                        dockerClient.removeImageCmd(imageName).exec()
                        logger.debug("Removed leftover image {}", imageName)
                    } catch (ignored: NotFoundException) {
                    } catch (e: Exception) {
                        logger.debug("Removing leftover image {} failed", imageName, e)
                        // only fail if not possible to delete an image that can interfere with the current test run
                        if (port == currentRegistryPort) {
                            throw e
                        }
                    }
                }
            }
        }
    }

    internal fun cleanup() {
        synchronized(OciImages) {
            try {
                cleanupImages()
            } finally {
                stopRegistry()
            }
        }
    }

    private fun cleanupImages() {
        if (imageNames.isEmpty()) {
            return
        }
        val dockerClient = DockerClientFactory.instance().client()
        var error: Exception? = null
        for ((imageName, retain) in imageNames) {
            if (retain) {
                try {
                    dockerClient.tagImageCmd(imageName.toString(), imageName.repository, imageName.versionPart).exec()
                    logger.debug("Retained image {}:{}", imageName.repository, imageName.versionPart)
                } catch (ignored: NotFoundException) {
                    logger.debug("Retaining image {}:{} failed", imageName.repository, imageName.versionPart, ignored)
                }
            }
            try {
                dockerClient.removeImageCmd(imageName.toString()).exec()
                logger.debug("Removed image {}", imageName)
            } catch (ignored: NotFoundException) {
            } catch (e: Exception) {
                logger.debug("Removing image {} failed", imageName, e)
                if (error == null) {
                    error = e
                } else {
                    error.addSuppressed(e)
                }
            }
        }
        imageNames.clear()
        if (error != null) {
            throw error
        }
    }

    private fun stopRegistry() {
        registry?.run {
            logger.debug("Stopping OCI registry on port {}", port())
            disposeNow()
            logger.debug("Stopped OCI registry")
            registry = null
        }
    }
}
