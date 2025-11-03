package org.app.corge

import androidx.compose.runtime.Composable

interface PlatformHandler {
    fun shareFile(filePath: String)
}

@Composable
expect fun getPlatformHandler(): PlatformHandler

expect fun createShareFile(content: String): String