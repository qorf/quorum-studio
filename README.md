# Quorum Studio #

This repository contains the source code for the Quorum Studio Programming environment, which is the development environment for the [Quorum programming language](https://github.com/qorf/quorum-language). Quorum Studio provides a series of features for Quorum, including:

1. Compiling, Running, and Debugging Quorum programs
2. Code completion
3. Scene editing for 2D and 3D scenes, integrated with Quorum's graphics engine
4. Text editing, project navigation, and other features
5. Basic Git support for cloning and working with projects
6. Support for different compilation targets, including Java Bytecode and JavaScript modes

# Building Quorum Studio #

In order to Build Quorum Studio, we need several components. To build it, we do the following steps:

1. Download [IntelliJ](https://www.jetbrains.com/idea/). This is not strictily necessary if we want to compile from the console, but provides support for Gradle built in, which Quorum Studio needs for its plugins.
2. Clone the [quorum-debugger](https://github.com/qorf/quorum-debugger)
3. Open the quorum-degugger repository and build the project by running the gradle script
4. Open the QuorumStudioPlugins folder as an IntelliJ project and run the Gradle script
5. Once all plugins are built, open Quorum Studio in Quorum Studio, then build it


