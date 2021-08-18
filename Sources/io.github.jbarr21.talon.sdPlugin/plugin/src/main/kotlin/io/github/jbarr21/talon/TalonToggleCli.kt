@file:JvmName("TalonToggleCli")

package io.github.jbarr21.talon

import io.github.jbarr21.streamdeck.StreamDeckCommand
import org.slf4j.LoggerFactory

fun main(args: Array<String>) {
  val logger = LoggerFactory.getLogger(TalonTogglePlugin::class.java.simpleName)
  logger.info("main")
  StreamDeckCommand(TalonTogglePlugin()).main(args)
}
