@file:JvmName("TalonToggleCli")

package io.github.jbarr21.talon

import io.github.jbarr21.streamdeck.StreamDeckCommand
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

fun main(args: Array<String>) = runBlocking {
  val logger = LoggerFactory.getLogger(TalonTogglePlugin::class.java.simpleName)
  logger.info("Starting TalonToggle plugin w/ args: ${args.joinToString(separator = ", ")}")
  StreamDeckCommand(TalonTogglePlugin(this)).main(args)
}
