package io.github.sgtsilvio.gradle.oci.junit.jupiter

import com.github.dockerjava.api.exception.NotFoundException
import com.sun.jna.Platform
import io.github.sgtsilvio.oci.registry.DefaultOciRegistryStorage
import io.github.sgtsilvio.oci.registry.OciRegistryHandler
import org.testcontainers.DockerClientFactory
import org.testcontainers.utility.DockerImageName
import reactor.netty.DisposableServer
import reactor.netty.http.server.HttpServer
import java.nio.file.Paths

/**
 * @author Silvio Giebl
 */
object OciImages {
    private var registry: DisposableServer? = null
    private val imageNames = mutableListOf<Pair<DockerImageName, Boolean>>()

    @JvmStatic
    fun getImageName(imageName: String) = getImageName(imageName, false)

    @JvmStatic
    fun getImageName(imageName: String, retain: Boolean): DockerImageName {
        synchronized(OciImages) {
            val port = startRegistry().port()
            val host = if (Platform.isMac() || Platform.isWindows()) "host.docker.internal" else "localhost"
            val dockerImageName = DockerImageName.parse("$host:$port/$imageName").asCompatibleSubstituteFor(imageName)
            imageNames += Pair(dockerImageName, retain)
            return dockerImageName
        }
    }

    private fun startRegistry(): DisposableServer = registry ?: run {
        val registryDataDirectory = Paths.get(System.getProperty("io.github.sgtsilvio.gradle.oci.registry.data.dir"))
        val registry = HttpServer.create()
            .port(0)
            .handle(OciRegistryHandler(DefaultOciRegistryStorage(registryDataDirectory)))
            .bindNow()
        this.registry = registry
        Runtime.getRuntime().addShutdownHook(Thread { cleanup() })
        registry
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

    private fun stopRegistry() {
        registry?.disposeNow()
        registry = null
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
                } catch (ignored: NotFoundException) {
                }
            }
            try {
                dockerClient.removeImageCmd(imageName.toString()).exec()
            } catch (ignored: NotFoundException) {
            } catch (e: Exception) {
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
}
