package io.github.jbarr21.talon

import com.github.andrewoma.kommon.script.shell
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.github.jbarr21.streamdeck.EventPayload
import io.github.jbarr21.streamdeck.SetStateEvent
import io.github.jbarr21.streamdeck.StreamDeckPlugin
import kotlinx.coroutines.*
import okhttp3.WebSocket
import org.slf4j.LoggerFactory

class TalonTogglePlugin : StreamDeckPlugin {

  private val talonRepl = "${System.getenv("HOME")}/.talon/bin/repl"
  private val logger = LoggerFactory.getLogger(TalonTogglePlugin::class.java.simpleName)
  private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

  private var intervalJob: Job? = null

  override fun onWillAppear(ws: WebSocket, context: String) {
    CoroutineScope(Dispatchers.IO).launchPeriodicAsync(1000L) {
      setState(ws, context, if (isTalonEnabled()) 1 else 0)
    }
  }

  override fun onKeyUp(ws: WebSocket, context: String) {
    val enabled = toggleTalon()
    setState(ws, context, if (enabled) 1 else 0)
  }

  override fun onWillDisappear(ws: WebSocket, context: String) {
    intervalJob?.cancel()
  }

  private fun setState(ws: WebSocket, context: String, state: Int) {
    logger.info("set state $state")
    moshi.adapter(SetStateEvent::class.java)
      .toJson(SetStateEvent("setState", context, EventPayload(state)))
      .also { ws.send(it) }
    logger.info("set state $state success")
  }

  private fun isTalonEnabled(): Boolean {
    val script = """
        |int(actions.speech.enabled())
        |quit()
        """.trimMargin()
    return runTalonReplScript(script)
  }

  private fun toggleTalon(): Boolean {
    val script = """
        |from talon import actions
        |
        |speech_enabled = actions.speech.enabled()
        |if speech_enabled:
        |    actions.speech.disable()
        |else:
        |    actions.speech.enable()
        |
        |speech_enabled = not speech_enabled
        |print(int(speech_enabled))
        |quit()
        """.trimMargin()
    return runTalonReplScript(script)
  }

  private fun runTalonReplScript(script: String): Boolean {
    return shell("echo '$script' | $talonRepl", verify = { true }).out
      .lines()
      .filterNot { "REPL" in it }
      .first()
      .trim()
      .toInt() > 0
  }
}

fun CoroutineScope.launchPeriodicAsync(delayMillis: Long, action: () -> Unit): Job {
  return async {
    if (delayMillis > 0) {
      while (isActive) {
        action()
        delay(delayMillis)
      }
    } else {
      action()
    }
  }
}
