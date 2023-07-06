package io.github.jbarr21.streamdeck;

import java.io.File

enum class TalonState(val stateId: Int, val color: Int) {
  SLEEP(0, 0x808080),
  COMMAND(1, 0x6495ed),
  DICTATION(2, 0xffd700),
  MIXED(3, 0x3cb371),
  OTHER(4, 0xf8f8ff);

  companion object {
    fun fromFile(file: File): TalonState {
      val modeText = file.readText().trim().toUpperCase()
      return if (modeText.isNotEmpty()) {
        TalonState.valueOf(modeText)
      } else {
        OTHER
      }
    }
  }
}
