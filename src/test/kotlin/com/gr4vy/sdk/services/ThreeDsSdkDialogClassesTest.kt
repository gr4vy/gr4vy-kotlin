package com.gr4vy.sdk.services

import com.netcetera.threeds.sdk.api.transaction.Transaction
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.zip.ZipFile
import android.app.DialogFragment as PlatformDialogFragment
import androidx.fragment.app.DialogFragment as AndroidxDialogFragment

/**
 * Pins what ADR 0008 relies on in the bundled 3DS SDK: it shows its screens as a platform dialog fragment or,
 * on an AppCompatActivity, an AndroidX one, and the sandbox screen capture recognises both.
 * If this fails after a 3DS SDK upgrade, update Gr4vySandboxChallengeScreenCapture and rerun the challenge
 * rows of docs/QA.md on both host activities.
 */
@Suppress("DEPRECATION")
class ThreeDsSdkDialogClassesTest {

    private val dialogClasses: List<Class<*>> by lazy {
        val location = Transaction::class.java.protectionDomain?.codeSource?.location
        val jar = File(requireNotNull(location) { "can't locate the bundled 3DS SDK jar" }.toURI())
        ZipFile(jar).use { zip ->
            zip.entries().asSequence()
                .map { it.name }
                .filter { it.endsWith(".class") }
                .mapNotNull { entry ->
                    // Load without initialising; skip classes whose dependencies aren't on the test classpath.
                    runCatching { Class.forName(entry.removeSuffix(".class").replace('/', '.'), false, javaClass.classLoader) }
                        .getOrNull()
                }
                .filter { it.isPlatformDialog() || it.isAndroidxDialog() }
                .toList()
        }
    }

    private fun Class<*>.isPlatformDialog() = PlatformDialogFragment::class.java.isAssignableFrom(this)
    private fun Class<*>.isAndroidxDialog() = AndroidxDialogFragment::class.java.isAssignableFrom(this)

    @Test
    fun `the 3DS SDK has both a platform and an AndroidX dialog fragment`() {
        assertTrue("no platform dialog fragment in the 3DS SDK", dialogClasses.any { it.isPlatformDialog() })
        assertTrue("no AndroidX dialog fragment in the 3DS SDK", dialogClasses.any { it.isAndroidxDialog() })
    }

    @Test
    fun `the sandbox screen capture recognises every 3DS SDK dialog fragment`() {
        val missed = dialogClasses.filterNot { Gr4vySandboxChallengeScreenCapture.isThreeDsSdkClass(it) }
        assertTrue("not recognised: ${missed.map { it.name }}", missed.isEmpty())
    }
}
