# Stream Deck Talon Plugin

A [Stream Deck](https://www.elgato.com/en/stream-deck-mini) plugin for [Talon Voice](https://talonvoice.com) that allows for easily toggling the speech engine and showing the current status.

## Building

In order to install the plugin from source:

Close the Stream Deck app
`killall 'Stream Deck'`

Stop the Talon Toggle plugin
`ps aux | grep -E "java -jar.*streamdeck-talon" | awk '{print $2}' | xargs kill -9`

Uninstall the plugin from Stream Deck
`rm -rf ~/Library/Application\ Support/com.elgato.StreamDeck/Plugins/io.github.jbarr21.talon.sdPlugin`

Delete the existing plugin release
`rm Release/io.github.jbarr21.talon.streamDeckPlugin`

Build the Kotlin CLI jar
```
pushd Sources/io.github.jbarr21.talon.sdPlugin/plugin
./gradlew clean build shadowJar
popd
```

Package the plugin sources using [DistributionTool](https://docs.elgato.com/sdk/plugins/packaging)
`DistributionTool -b -i Sources/io.github.jbarr21.talon.sdPlugin -o Release/`

Open Stream Deck and install the plugin
`open Release/io.github.jbarr21.talon.streamDeckPlugin`

Logs are viewable in:
`~/streamdeck-plugin.{TODAYS_DATE_HERE}.log`
