package io.github.jbarr21.talon

import com.github.andrewoma.kommon.script.shell
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.github.irgaly.kfswatch.KfsDirectoryWatcher
import io.github.jbarr21.streamdeck.EventPayload
import io.github.jbarr21.streamdeck.SetStateEvent
import io.github.jbarr21.streamdeck.StreamDeckPlugin
import io.github.jbarr21.streamdeck.TalonState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.WebSocket
import org.slf4j.LoggerFactory
import java.io.File

class TalonTogglePlugin(scope: CoroutineScope) : StreamDeckPlugin(scope) {

  private val talonRepl = "${System.getenv("HOME")}/.talon/bin/repl"
  private val talonModeFile = File("${System.getenv("HOME")}/.talon/talon.mode")
  private val logger by lazy { LoggerFactory.getLogger(TalonTogglePlugin::class.java.simpleName) }
  private val watcher by lazy { KfsDirectoryWatcher(scope, Dispatchers.IO) }
  private val moshi by lazy {
    Moshi.Builder()
      .add(KotlinJsonAdapterFactory())
      .build()
  }

  override fun onWillAppear(ws: WebSocket, context: String) {
    setState(ws, context, TalonState.fromFile(talonModeFile))
    scope.launch(Dispatchers.IO) {
      watchTalonMode(ws, context)
    }
  }

  private suspend fun watchTalonMode(ws: WebSocket, context: String) {
    if (watcher.watchingDirectories.isEmpty()) {
      watcher.add(talonModeFile.parentFile.absolutePath)
    }
    watcher.onEventFlow
      .filter { it.path.endsWith(talonModeFile.name) }
      .map { TalonState.fromFile(talonModeFile) }
      .distinctUntilChanged()
      .collect { state ->
        setState(ws, context, state)
      }
  }

  override fun onKeyUp(ws: WebSocket, context: String) {
    logger.info("toggle button pressed")
    toggleTalon()
    val state = talonState()
    logger.info("new talon state is = $state")
    setState(ws, context, state)
  }

  override fun onWillDisappear(ws: WebSocket, context: String) { }

  private fun setState(ws: WebSocket, context: String, state: TalonState) {
    logger.info("setting state to $state")
    val setStateEvent = SetStateEvent("setState", context, EventPayload(state.ordinal))
    moshi.adapter(SetStateEvent::class.java)
      .toJson(setStateEvent)
      .also { ws.send(it) }
  }

  private fun talonState(): TalonState {
    val script = """
        |from talon import actions
        |
        |modes = scope.get("mode")
        |if "sleep" in modes:
        |    print(${TalonState.SLEEP.ordinal})
        |elif "dictation" in modes:
        |    if "command" in modes:
        |        print(${TalonState.MIXED.ordinal})
        |    else:
        |        print(${TalonState.DICTATION.ordinal})
        |elif "command" in modes:
        |    print(${TalonState.COMMAND.ordinal})
        |
        |quit()
        """.trimMargin()
    val output = runTalonReplScript(script)
    return TalonState.values()[output]
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
        |
        |quit()
        """.trimMargin()
    val output = runTalonReplScript(script)
    logger.info("toggleTalon: speech is = $output")
    return output > 0
  }

  private fun runTalonReplScript(script: String): Int {
    return shell("echo '$script' | $talonRepl", verify = { true }).out
      .lines()
      .filterNot { "REPL" in it }
      .first()
      .trim()
      .toInt()
  }
}


