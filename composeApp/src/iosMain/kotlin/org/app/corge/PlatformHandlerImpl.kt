package org.app.corge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.Foundation.*
import platform.UIKit.UIViewController
import platform.posix.memcpy

class PlatformHandlerImpl : PlatformHandler {

    override fun shareFile(filePath: String) {
        val fileUrl = NSURL.fileURLWithPath(filePath)

        if (fileUrl == null) {
            println("⚠️ Invalid file path for sharing: $filePath")
            return
        }

        val activityViewController = UIActivityViewController(
            activityItems = listOf(fileUrl),
            applicationActivities = null
        )

        val rootController = getTopViewController()
        rootController?.presentViewController(activityViewController, animated = true, completion = null)
            ?: println("⚠️ Failed to find root controller for sharing")
    }

    private fun getTopViewController(): UIViewController? {
        val keyWindow = UIApplication.sharedApplication.keyWindow
        var topController = keyWindow?.rootViewController

        while (topController?.presentedViewController != null) {
            topController = topController?.presentedViewController
        }
        return topController
    }
}

@Composable
actual fun getPlatformHandler(): PlatformHandler = remember { PlatformHandlerImpl() }

@OptIn(ExperimentalForeignApi::class)
actual fun createShareFile(content: String): String {
    val tempDir = NSTemporaryDirectory()
    val fileName = "vinsprit_share_${NSDate().timeIntervalSince1970}.txt"
    val filePath = tempDir + fileName

    val bytes = content.encodeToByteArray()
    val nsData = bytes.toNSData()
    nsData.writeToFile(filePath, atomically = true)

    return filePath
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData = memScoped {
    val ptr = allocArray<ByteVar>(this@toNSData.size)
    memcpy(ptr, this@toNSData.refTo(0), this@toNSData.size.convert())
    return NSData.create(bytes = ptr, length = this@toNSData.size.toULong())
}