package org.app.corge.network

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.darwin.dispatch_queue_create

class NetworkMonitorIos : NetworkMonitor {

    private val monitor = nw_path_monitor_create()
    private val queue = dispatch_queue_create("NetworkMonitorQueue", null)

    private val _isConnected = MutableStateFlow(true)
    override val isConnected: StateFlow<Boolean> = _isConnected

    override fun start() {
        nw_path_monitor_set_queue(monitor, queue)

        nw_path_monitor_set_update_handler(monitor) { path ->
            val status = nw_path_get_status(path)
            val connected = status == nw_path_status_satisfied
            _isConnected.value = connected
        }

        nw_path_monitor_start(monitor)
    }

    override fun stop() {
        nw_path_monitor_cancel(monitor)
    }
}

actual fun createNetworkMonitor(): NetworkMonitor = NetworkMonitorIos()

class NetworkViewModel(
    private val monitor: NetworkMonitor = createNetworkMonitor()
) : ViewModel() {

    val isConnected = monitor.isConnected

    init {
        monitor.start()
    }

    override fun onCleared() {
        monitor.stop()
        super.onCleared()
    }
}

@Composable
fun NoInternetOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                "No Internet connection",
                color = Color.White,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            CircularProgressIndicator(color = Color.White)
        }
    }
}