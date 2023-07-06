package io.github.jbarr21.streamdeck

import kotlinx.coroutines.CoroutineScope
import okhttp3.WebSocket

abstract class StreamDeckPlugin(val scope: CoroutineScope) {

  open fun onDeviceDidConnect(ws: WebSocket, context: String) { }

  open fun onWillAppear(ws: WebSocket, context: String) { }

  open fun onKeyDown(ws: WebSocket, context: String) { }

  open fun onKeyUp(ws: WebSocket, context: String) { }

  open fun onWillDisappear(ws: WebSocket, context: String) { }

  open fun onDeviceDidDisconnect(ws: WebSocket, context: String) { }

  open fun onSystemDidWakeUp(ws: WebSocket, context: String) { }
}
