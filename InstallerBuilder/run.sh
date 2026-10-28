#!/bin/sh
# Runs the installer builder's window outside Quorum Studio. Build the
# project in Studio first, so Run/QuorumStudioInstaller.jar is current.

# It reads Settings.json and writes its log here, so run from this folder.
cd "$(dirname "$0")" || exit 1

# Studio's own JDK, the one the project is built and run with.
JAVA="../QuorumStudio/Java/jdk/Contents/Home/bin/java"
if [ ! -x "$JAVA" ]; then
    JAVA="java"
fi

# On a Mac, the window has to start on the first thread.
exec "$JAVA" -XstartOnFirstThread -jar Run/QuorumStudioInstaller.jar "$@"
