#!/bin/sh
set -e
killall 'Stream Deck' || true
ps aux | grep -E "java \-jar.*streamdeck-talon" | awk '{print $2}' | xargs kill -9 || true
rm -rf ~/Library/Application\ Support/com.elgato.StreamDeck/Plugins/io.github.jbarr21.talon.sdPlugin
rm -f Release/io.github.jbarr21.talon.streamDeckPlugin

pushd Sources/io.github.jbarr21.talon.sdPlugin/plugin
./gradlew clean shadowJar
popd

DistributionTool -b -i Sources/io.github.jbarr21.talon.sdPlugin -o Release/

open Release/io.github.jbarr21.talon.streamDeckPlugin

printf "%s " "Press enter after plugin is installed..."
read ans

killall 'Stream Deck' || true
open '/Applications/Elgato Stream Deck.app'
