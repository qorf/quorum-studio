# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_definitions(-DFT2_BUILD_LIBRARY)

# Specify library include directories
include_directories("src/main/cpp/freetype/include")

file(GLOB PUBLIC_HEADERS "src/main/cpp/freetype/include/*.h")
file(GLOB PUBLIC_CONFIG_HEADERS "src/main/cpp/freetype/include/config/*.h")
file(GLOB PRIVATE_HEADERS "src/main/cpp/freetype/include/internal/*.h")

add_library( # Sets the name of the library.
             GameEngineCPlugins

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/BufferUtils.cpp
             src/main/cpp/BufferUtils.h
             src/main/cpp/gdx2d.c
             src/main/cpp/gdx2d.h
             src/main/cpp/jpgd.cpp
             src/main/cpp/jpgd.h
             src/main/cpp/jpgd_c.cpp
             src/main/cpp/jpgd_c.h
             src/main/cpp/Matrix4.cpp
             src/main/cpp/Matrix4.h
             src/main/cpp/PixelMap.cpp
             src/main/cpp/PixelMap.h
             src/main/cpp/stb_image.h
	     ${PUBLIC_HEADERS}
  ${PUBLIC_CONFIG_HEADERS}
  ${PRIVATE_HEADERS}
  src/main/cpp/freetype/src/autofit/autofit.c
  src/main/cpp/freetype/src/base/ftadvanc.c
  src/main/cpp/freetype/src/base/ftbbox.c
  src/main/cpp/freetype/src/base/ftbdf.c
  src/main/cpp/freetype/src/base/ftbitmap.c
  src/main/cpp/freetype/src/base/ftcalc.c
  src/main/cpp/freetype/src/base/ftcid.c
  src/main/cpp/freetype/src/base/ftdbgmem.c
  src/main/cpp/freetype/src/base/ftdebug.c
  src/main/cpp/freetype/src/base/ftfstype.c
  src/main/cpp/freetype/src/base/ftgasp.c
  src/main/cpp/freetype/src/base/ftgloadr.c
  src/main/cpp/freetype/src/base/ftglyph.c
  src/main/cpp/freetype/src/base/ftgxval.c
  src/main/cpp/freetype/src/base/ftinit.c
  src/main/cpp/freetype/src/base/ftlcdfil.c
  src/main/cpp/freetype/src/base/ftmm.c
  src/main/cpp/freetype/src/base/ftobjs.c
  src/main/cpp/freetype/src/base/ftotval.c
  src/main/cpp/freetype/src/base/ftoutln.c
  src/main/cpp/freetype/src/base/ftpatent.c
  src/main/cpp/freetype/src/base/ftpfr.c
  src/main/cpp/freetype/src/base/ftrfork.c
  src/main/cpp/freetype/src/base/ftsnames.c
  src/main/cpp/freetype/src/base/ftstream.c
  src/main/cpp/freetype/src/base/ftstroke.c
  src/main/cpp/freetype/src/base/ftsynth.c
  src/main/cpp/freetype/src/base/ftsystem.c
  src/main/cpp/freetype/src/base/fttrigon.c
  src/main/cpp/freetype/src/base/fttype1.c
  src/main/cpp/freetype/src/base/ftutil.c
  src/main/cpp/freetype/src/base/ftwinfnt.c
  src/main/cpp/freetype/src/base/ftxf86.c
  src/main/cpp/freetype/src/bdf/bdf.c
  src/main/cpp/freetype/src/bzip2/ftbzip2.c
  src/main/cpp/freetype/src/cache/ftcache.c
  src/main/cpp/freetype/src/cff/cff.c
  src/main/cpp/freetype/src/cid/type1cid.c
  src/main/cpp/freetype/src/gzip/ftgzip.c
  src/main/cpp/freetype/src/lzw/ftlzw.c
  src/main/cpp/freetype/src/pcf/pcf.c
  src/main/cpp/freetype/src/pfr/pfr.c
  src/main/cpp/freetype/src/psaux/psaux.c
  src/main/cpp/freetype/src/pshinter/pshinter.c
  src/main/cpp/freetype/src/psnames/psmodule.c
  src/main/cpp/freetype/src/raster/raster.c
  src/main/cpp/freetype/src/sfnt/sfnt.c
  src/main/cpp/freetype/src/smooth/smooth.c
  src/main/cpp/freetype/src/truetype/truetype.c
  src/main/cpp/freetype/src/type1/type1.c
  src/main/cpp/freetype/src/type42/type42.c
  src/main/cpp/freetype/src/winfonts/winfnt.c

)

include_directories("src/truetype")
include_directories("src/sfnt")
include_directories("src/autofit")
include_directories("src/smooth")
include_directories("src/raster")
include_directories("src/psaux")
include_directories("src/psnames")

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       GameEngineCPlugins

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )