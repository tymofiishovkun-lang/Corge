package org.app.corge.network

import kotlinx.coroutines.flow.StateFlow

interface NetworkMonitor {
    val isConnected: StateFlow<Boolean>
    fun start()
    fun stop()
}

expect fun createNetworkMonitor(): NetworkMonitor