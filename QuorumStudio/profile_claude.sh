#!/bin/sh
# Runs the game under the YourKit profiler with CPU sampling turned on from the
# start. When the game window is closed, YourKit saves a .snapshot file into
# the snapshots folder next to this script, where Claude can open it through
# YourKit's MCP server.
#
# This uses the YourKit 2026.9 agent, so the snapshot matches the version of
# the MCP server that reads it.

cd "$(dirname "$0")" || exit 1

JAVA="/Applications/QuorumStudio.app/Contents/MacOS/Java/jdk/Contents/Home/bin/java"
AGENT="/Applications/YourKit Java Profiler.app/Contents/Resources/bin/mac/libyjpagent.dylib"
SNAPSHOTS="$(pwd)/snapshots"

mkdir -p "$SNAPSHOTS"

# sampling          start CPU sampling the moment the JVM starts, before
#                   Main runs, so loading and CreateGame are all recorded
# onexit=snapshot   save a snapshot when the program exits
# dir=...           where to save it
"$JAVA" -XstartOnFirstThread \
    "-agentpath:$AGENT=sampling,onexit=snapshot,dir=$SNAPSHOTS" \
    -jar Run/Default.jar
