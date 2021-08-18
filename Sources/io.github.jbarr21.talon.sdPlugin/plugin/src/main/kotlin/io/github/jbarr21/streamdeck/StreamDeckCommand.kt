package io.github.jbarr21.streamdeck

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import org.slf4j.LoggerFactory

class StreamDeckCommand(private val streamDeckPlugin: StreamDeckPlugin) : CliktCommand() {
  private val port: Int by option("-port").int().required()
  private val uuid: String by option("-pluginUUID").required()
  private val registerEvent: String by option("-registerEvent").required()
  private val info: String? by option("-info")

  private val logger = LoggerFactory.getLogger(StreamDeckCommand::class.java.simpleName)
  private val client = OkHttpClient()
  private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

  override fun run() {
    start()
  }

  private fun start() = runBlocking {
    logger.info("args: port=$port, uuid=$uuid, registerEvent=$registerEvent")
    client.webSocketEventFlow("ws://localhost:$port")
      .onEach {
        logger.info("socket msg type: ${it.javaClass.simpleName}")
        when (it) {
          is WebSocketEvent.Open -> onOpen(it.ws)
          is WebSocketEvent.Message -> onMessage(it.ws, it.message)
          is WebSocketEvent.Failure -> onFailure(it.t)
          is WebSocketEvent.Close -> onClose()
        }
      }
      .launchIn(this)
  }

  private fun onOpen(ws: WebSocket) {
    logger.info("opened ws")
    ws.send(moshi.adapter(RegisterEvent::class.java).toJson(RegisterEvent(registerEvent, uuid)))
    logger.info("registered")
  }

  private fun onMessage(ws: WebSocket, message: String) {
    logger.info("received msg")
    moshi.adapter(MessageEvent::class.java).fromJson(message)?.let {
      logger.info("event: ${it.event}")
      val context = it.context.orEmpty()
      when (it.event) {
        "willAppear" -> streamDeckPlugin.onWillAppear(ws, context)
        "keyUp" -> streamDeckPlugin.onKeyUp(ws, context)
        "willDisappear" -> streamDeckPlugin.onWillDisappear(ws, context)
      }
    } ?: throw IllegalStateException("Could not parse json for message")
  }

  private fun onFailure(t: Throwable) {
    logger.info("failure: $t")
  }

  private fun onClose() {
    logger.info("close")
  }
}

data class RegisterEvent(val event: String, val uuid: String)
data class MessageEvent(val event: String, val context: String?)
data class SetStateEvent(val event: String, val context: String, val payload: EventPayload)
data class EventPayload(val state: Int)