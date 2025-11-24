package org.app.corge

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import corge.composeapp.generated.resources.Res
import corge.composeapp.generated.resources.nav_back
import corge.composeapp.generated.resources.nav_forward
import corge.composeapp.generated.resources.nav_home
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import org.app.corge.network.NetworkViewModel
import org.app.corge.network.NoInternetOverlay
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun WebScreen(url: String) {

    println("🔵 Safe WebScreen START: $url")

    val webViewState = remember { mutableStateOf<WKWebView?>(null) }

    val networkVM: NetworkViewModel = koinInject()
    val isConnected by networkVM.isConnected.collectAsState()

    Box(Modifier.fillMaxSize()) {

        UIKitView(
            modifier = Modifier.fillMaxSize(),
            factory = {

                println("🟡 Creating minimal WKWebView (SAFE MODE)")

                val webView = WKWebView(
                    frame = cValue { CGRectMake(0.0, 0.0, 0.0, 0.0) },
                    configuration = WKWebViewConfiguration()
                )

                webViewState.value = webView

                println("🟡 Loading URL → $url")
                webView.loadRequest(
                    NSURLRequest(NSURL(string = url))
                )

                webView
            },
            update = { view ->
                webViewState.value?.setFrame(view.bounds)
            }
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(56.dp)
                .background(Color.DarkGray.copy(alpha = 0.8f)),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                painter = painterResource(Res.drawable.nav_back),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        println("🔵 Back pressed")
                        webViewState.value?.goBack()
                    }
            )

            Icon(
                painter = painterResource(Res.drawable.nav_home),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        println("🔵 Home pressed")
                        val w = webViewState.value
                        if (w != null) {
                            w.loadRequest(NSURLRequest(NSURL(string = url)))
                        }
                    }
            )

            Icon(
                painter = painterResource(Res.drawable.nav_forward),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        println("🔵 Forward pressed")
                        webViewState.value?.goForward()
                    }
            )
        }

        if (!isConnected) {
            NoInternetOverlay()
        }
    }
}