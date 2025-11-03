package org.app.corge.pdfExporter

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.*
import platform.UIKit.*
import platform.CoreGraphics.*
import kotlinx.coroutines.Dispatchers
import platform.Foundation.NSSet
import kotlinx.coroutines.withContext
import platform.Foundation.NSURL.Companion.fileURLWithPath

@OptIn(ExperimentalForeignApi::class)
actual class PdfExporter actual constructor() {

    actual suspend fun export(payload: ExportPayload, fileName: String): ExportResult =
        withContext(Dispatchers.Default) {
            try {
                val fm = NSFileManager.defaultManager
                val docsUrl = (fm.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
                    ?.firstOrNull() as? NSURL)
                    ?: return@withContext ExportResult.Error("Documents folder not found")
                val fileUrl = docsUrl.URLByAppendingPathComponent("$fileName.pdf")
                    ?: return@withContext ExportResult.Error("Failed to build file URL")

                val format = UIGraphicsPDFRendererFormat()
                val bounds = CGRectMake(0.0, 0.0, 595.0, 842.0)
                val renderer = UIGraphicsPDFRenderer(bounds, format)

                val data = renderer.PDFDataWithActions { ctx ->
                    ctx?.beginPage()

                    val cg = UIGraphicsGetCurrentContext()
                    if (cg != null) {
                        CGContextSetRGBFillColor(cg, 1.0, 1.0, 1.0, 1.0)
                        CGContextFillRect(cg, bounds)
                    }

                    fun draw(text: String, x: Double, y: Double, font: UIFont, color: UIColor = UIColor.blackColor) {
                        val attrs: Map<Any?, Any?> = mapOf(
                            NSFontAttributeName to font,
                            NSForegroundColorAttributeName to color
                        )
                        (text as NSString).drawAtPoint(CGPointMake(x, y), attrs)
                    }

                    var y = 60.0
                    val x = 48.0
                    draw("Stats (last ${payload.periodDays} days)", x, y, UIFont.boldSystemFontOfSize(20.0))
                    y += 28.0
                    draw("Generated: ${payload.generatedAt}", x, y, UIFont.systemFontOfSize(10.0), UIColor.grayColor())
                    y += 28.0

                    fun metric(label: String, value: String) {
                        draw(label, x, y, UIFont.systemFontOfSize(10.0), UIColor.grayColor()); y += 16.0
                        draw(value, x, y, UIFont.systemFontOfSize(12.0)); y += 24.0
                    }
                    metric("Streak", payload.streak.toString())
                    metric("Total Days Done", payload.totalDaysDone.toString())
                    metric("Avg Session", formatDuration(payload.avgDurationSec.toInt()))

                    y += 8.0
                    draw("By Category:", x, y, UIFont.systemFontOfSize(12.0)); y += 16.0
                    payload.shares.forEach { s ->
                        draw("• ${s.label}: ${s.count}", x, y, UIFont.systemFontOfSize(12.0)); y += 16.0
                    }
                }

                val ok = data.writeToURL(fileUrl, true)
                if (!ok) return@withContext ExportResult.Error("Failed to write PDF")

                ExportResult.Ok(fileUrl.path ?: fileName)
            } catch (t: Throwable) {
                ExportResult.Error("Export failed: ${t.message}")
            }
        }
}

private fun formatDuration(sec: Int): String {
    if (sec <= 0) return "–"
    val m = sec / 60
    val s = sec % 60
    return "$m:${s.toString().padStart(2, '0')}"
}

@OptIn(ExperimentalForeignApi::class)
class IOSExportViewer : ExportViewer {
    override fun view(location: String) {
        val url = NSURL.fileURLWithPath(path = location)

        val activity = UIActivityViewController(
            activityItems = listOf(url),
            applicationActivities = null
        )

        val root = topViewController() ?: return

        activity.popoverPresentationController?.let { pop ->
            pop.sourceView = root.view
            pop.sourceRect = CGRectMake(0.0, 0.0, 1.0, 1.0)
            pop.permittedArrowDirections = 0U
        }

        root.presentViewController(activity, true, null)
    }
}

actual fun exportFolderHint(): String = "Files → On My iPhone → YourApp"

private fun topViewController(): UIViewController? {
    val app = UIApplication.sharedApplication

    val scene = (app.connectedScenes as? NSSet)?.anyObject() as? UIScene
    if (scene is UIWindowScene) {
        val win: UIWindow? = firstFromCollection(scene.windows)
        val vc = win?.rootViewController
        if (vc != null) return vc.topPresented()
    }

    val win2: UIWindow? = firstFromCollection(app.windows)
    return win2?.rootViewController?.topPresented()
}

private inline fun <reified T> firstFromCollection(collection: Any?): T? = when (collection) {
    is List<*> -> collection.firstOrNull() as? T
    is NSArray -> collection.firstObject as? T
    else -> null
}

private fun UIViewController.topPresented(): UIViewController {
    var cur: UIViewController = this
    while (cur.presentedViewController != null) {
        cur = cur.presentedViewController!!
    }
    return cur
}