#!/bin/sh
java -XstartOnFirstThread -agentpath:/Applications/YourKit-Java-Profiler-2022.3.app/Contents/Resources/bin/mac/libyjpagent.dylib=alloc_object_counting -jar Run/QuorumStudio.jar
