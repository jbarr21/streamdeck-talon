package io.github.jbarr21.streamdeck

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.*
import okio.ByteString
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal val OkHttpClient.logger: Logger
  get() = LoggerFactory.getLogger(WebSocket::class.java.simpleName)

internal fun OkHttpClient.webSocketEventFlow(url: String): Flow<WebSocketEvent> = callbackFlow {
  val webSocketListener = object : WebSocketListener() {
    override fun onOpen(ws: WebSocket, response: Response) {
      logger.info("onOpen $response")
      trySendBlocking(WebSocketEvent.Open(ws))
    }

    override fun onMessage(ws: WebSocket, text: String) {
      logger.info("onMessage($text)")
      trySendBlocking(WebSocketEvent.Message(ws, text))
    }

    override fun onMessage(ws: WebSocket, bytes: ByteString) {
      logger.info("onMessage($bytes)")
      trySendBlocking(WebSocketEvent.Message(ws, bytes.toString()))
    }

    override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
      logger.info("onFailure($t, $response)")
      trySendBlocking(WebSocketEvent.Failure(ws, t))
    }

    override fun onClosed(ws: WebSocket, code: Int, reason: String) {
      logger.info("onClosed($code, $reason)")
      trySendBlocking(WebSocketEvent.Close(ws, code, reason))
    }
  }

  val ws = newWebSocket(Request.Builder().url(url).build(), webSocketListener)
  awaitClose { ws.cancel() }
}

sealed class WebSocketEvent {
  data class Open(val ws: WebSocket) : WebSocketEvent()
  data class Message(val ws: WebSocket, val message: String) : WebSocketEvent()
  data class Failure(val ws: WebSocket, val t: Throwable) : WebSocketEvent()
  data class Close(val ws: WebSocket, val code: Int, val reason: String) : WebSocketEvent()
}
