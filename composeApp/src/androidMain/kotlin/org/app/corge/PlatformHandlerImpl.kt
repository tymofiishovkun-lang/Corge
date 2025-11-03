package org.app.corge

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

class PlatformHandlerImpl(private val context: Context) : PlatformHandler {

    override fun shareFile(filePath: String) {
        try {
            val csvText = filePath

            val file = File(context.cacheDir, "export_history.csv")
            file.writeText(csvText)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Export History"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

@Composable
actual fun getPlatformHandler(): PlatformHandler {
    val context = LocalContext.current
    return remember { PlatformHandlerImpl(context) }
}

actual fun createShareFile(content: String): String {
    val tempDir = File(System.getProperty("java.io.tmpdir"))
    val file = File(tempDir, "corge_share_${System.currentTimeMillis()}.txt")
    file.writeText(content)
    return file.absolutePath
}