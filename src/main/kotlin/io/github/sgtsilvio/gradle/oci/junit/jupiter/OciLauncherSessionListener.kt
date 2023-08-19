package io.github.sgtsilvio.gradle.oci.junit.jupiter

import org.junit.platform.launcher.LauncherSession
import org.junit.platform.launcher.LauncherSessionListener

/**
 * @author Silvio Giebl
 */
class OciLauncherSessionListener : LauncherSessionListener {
    override fun launcherSessionClosed(session: LauncherSession) = OciImages.cleanup()
}
