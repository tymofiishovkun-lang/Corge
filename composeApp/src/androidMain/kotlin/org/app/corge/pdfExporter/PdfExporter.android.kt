package org.app.corge.pdfExporter

import android.content.ContentValues
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.app.corge.AppContextProvider

actual class PdfExporter actual constructor() {

    private val context get() = AppContextProvider.get()

    @RequiresApi(Build.VERSION_CODES.Q)
    actual suspend fun export(payload: ExportPayload, fileName: String): ExportResult =
        withContext(Dispatchers.IO) {
            try {
                val doc = android.graphics.pdf.PdfDocument()
                val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
                val page = doc.startPage(pageInfo)
                val c = page.canvas

                c.drawColor(Color.WHITE)

                val title = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.BLACK
                    textSize = 20f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }
                val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.BLACK
                    textSize = 12f
                }
                val gray = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(120, 120, 120)
                    textSize = 10f
                }

                var y = 60f
                val x = 48f
                c.drawText("Stats (last ${payload.periodDays} days)", x, y, title)
                y += 8f
                c.drawText("Generated: ${payload.generatedAt}", x, y + 16f, gray); y += 36f

                fun drawMetric(label: String, value: String, top: Float) {
                    val box = RectF(x, top, x + 200f, top + 64f)
                    val bg = Paint().apply { color = Color.rgb(245, 245, 245) }
                    val stroke = Paint().apply {
                        color = Color.rgb(220, 220, 220)
                        style = Paint.Style.STROKE
                        strokeWidth = 1f
                    }
                    c.drawRoundRect(box, 10f, 10f, bg)
                    c.drawRoundRect(box, 10f, 10f, stroke)
                    c.drawText(label, box.left + 12f, box.top + 24f, gray)
                    c.drawText(value, box.left + 12f, box.top + 46f, text)
                }

                drawMetric("Streak", payload.streak.toString(), y); y += 80f
                drawMetric("Total Days Done", payload.totalDaysDone.toString(), y); y += 80f
                drawMetric("Avg Session", formatDurationKmp(payload.avgDurationSec), y); y += 100f

                c.drawText("By Category:", x, y, text); y += 16f
                payload.shares.forEach { s ->
                    c.drawText("• ${s.label}: ${s.count}", x, y, text)
                    y += 16f
                }

                doc.finishPage(page)

                val collection =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    else
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI

                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/YourApp")
                    }
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(collection, values)
                    ?: return@withContext ExportResult.Error("Cannot create file in Downloads")

                resolver.openOutputStream(uri)?.use { out ->
                    doc.writeTo(out)
                } ?: return@withContext ExportResult.Error("Cannot open output stream")

                doc.close()
                ExportResult.Ok(uri.toString())
            } catch (t: Throwable) {
                ExportResult.Error("Export failed: ${t.message}")
            }
        }
}

private fun formatDurationKmp(sec: Int): String {
    if (sec <= 0) return "–"
    val m = sec / 60
    val s = (sec % 60).toString().padStart(2, '0')
    return "$m:$s"
}

actual fun exportFolderHint(): String = "Downloads/"

class AndroidExportViewer(
    private val context: android.content.Context
) : ExportViewer {

    override fun view(location: String) {
        val uri = android.net.Uri.parse(location)
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
            .onFailure {
                val chooser = android.content.Intent.createChooser(intent, "Open PDF")
                    .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            }
    }
}
