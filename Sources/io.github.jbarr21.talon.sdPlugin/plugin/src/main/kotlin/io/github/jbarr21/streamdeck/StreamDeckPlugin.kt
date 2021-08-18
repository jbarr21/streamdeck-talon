package io.github.jbarr21.streamdeck

import okhttp3.WebSocket

interface StreamDeckPlugin {
  fun onWillAppear(ws: WebSocket, context: String)

  fun onKeyUp(ws: WebSocket, context: String)

  fun onWillDisappear(ws: WebSocket, context: String)
}