#!/bin/sh
export METAL_CAPTURE_ENABLED=1
export MTL_DEBUG_LAYER=1
export MTL_DEBUG_LAYER_ERROR_MODE="ignore"
java -XstartOnFirstThread -jar Run/QuorumStudio.jar