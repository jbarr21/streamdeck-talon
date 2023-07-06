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

  private var lastState = TalonState.SLEEP

  private var intervalJob: Job? = null

  override fun onWillAppear(ws: WebSocket, context: String) {
    lastState = talonState()
    setState(ws, context, lastState)

//    CoroutineScope(Dispatchers.IO).launchPeriodicAsync(1000L) {
//      lastState = talonState()
//      setState(ws, context, lastState)
//    }
  }

  override fun onKeyUp(ws: WebSocket, context: String) {
    toggleTalon()
    val state = talonState()
    setState(ws, context, state)
  }

  override fun onWillDisappear(ws: WebSocket, context: String) {
    intervalJob?.cancel()
  }

  private fun setState(ws: WebSocket, context: String, state: TalonState) {
    logger.info("setting state to $state")
    moshi.adapter(SetStateEvent::class.java)
      .toJson(SetStateEvent("setState", context, EventPayload(state.ordinal)))
      .also { ws.send(it) }
    logger.info("set state $state success")
  }

  private fun talonState(): TalonState {
    val script = """
        |from talon import actions
        |
        |modes = scope.get("mode")
        |if "sleep" in modes:
        |    print(${TalonState.SLEEP.ordinal})
        |elif "dictation" in modes        |    if "command" in modes:
        |rg
        |        print(${TalonState.DICTATION.ordinal})
        |    else:
        |        print(${TalonState.MIXED.ordinal})
        |elif "command" in modes:
        |    print(${TalonState.COMMAND.ordinal})
        |quit()
        """.trimMargin()
    val output = runTalonReplScript(script)
    logger.info("talonState = $output")
    return TalonState.values()[output]
  }

  private fun toggleTalon(): TalonState {
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
    val output = runTalonReplScript(script)
    logger.info("toggleTalon: speech is = $output")
    return TalonState.values()[output]
  }

  private fun runTalonReplScript(script: String): Int {
    logger.info("running talon script: $script")
    return shell("echo '$script' | $talonRepl", verify = { true }).out
      .lines()
      .filterNot { "REPL" in it }
      .first()
      .trim()
      .toInt()
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

enum class TalonState {
  SLEEP,
  COMMAND,
  DICTATION,
  MIXED,
}
