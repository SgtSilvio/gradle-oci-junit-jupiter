package io.github.sgtsilvio.gradle.oci.junit.jupiter

import com.github.dockerjava.api.exception.NotFoundException
import com.sun.jna.Platform
import io.github.sgtsilvio.oci.registry.OciRegistryHandler
import org.junit.platform.launcher.LauncherSession
import org.junit.platform.launcher.LauncherSessionListener
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
        val port = startRegistry().port()
        val host = if (Platform.isMac()) "host.docker.internal" else "localhost"
        val dockerImageName = DockerImageName.parse("$host:$port/$imageName").asCompatibleSubstituteFor(imageName)
        imageNames += Pair(dockerImageName, retain)
        return dockerImageName
    }

    private fun startRegistry(): DisposableServer = registry ?: HttpServer.create().port(0)
        .handle(OciRegistryHandler(Paths.get(System.getProperty("io.github.sgtsilvio.gradle.oci.registry.data.dir"))))
        .bindNow().also { registry = it }

    private fun stopRegistry() {
        registry?.disposeNow()
        registry = null
    }

    private fun cleanupImages() {
        val dockerClient = DockerClientFactory.instance().client()
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
            }
        }
        imageNames.clear()
    }

    internal fun cleanup() {
        stopRegistry()
        cleanupImages()
    }
}

class OciLauncherSessionListener : LauncherSessionListener {
    override fun launcherSessionClosed(session: LauncherSession) = OciImages.cleanup()
}
