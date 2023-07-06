#!/bin/sh
set -e

# Close the app, stop the Talon Toggle plugin, & uninstall the plugin
killall 'Stream Deck' || true
ps aux | grep -E "java \-jar.*streamdeck-talon" | awk '{print $2}' | xargs kill -9 || true
rm -rf ~/Library/Application\ Support/com.elgato.StreamDeck/Plugins/io.github.jbarr21.talon.sdPlugin
rm -f Release/io.github.jbarr21.talon.streamDeckPlugin

# Build the plugin JAR
pushd Sources/io.github.jbarr21.talon.sdPlugin/plugin
./gradlew clean shadowJar
popd

# Package & install the Stream Deck plugin
# DistributionTool is available at: https://docs.elgato.com/sdk/plugins/packaging
DistributionTool -b -i Sources/io.github.jbarr21.talon.sdPlugin -o Release/
open Release/io.github.jbarr21.talon.streamDeckPlugin

printf "%s " "Press enter after plugin is installed..."
read ans

killall 'Stream Deck' || true
open '/Applications/Elgato Stream Deck.app'
