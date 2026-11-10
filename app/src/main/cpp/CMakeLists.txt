cmake_minimum_required(VERSION 3.22)

project(maxfreeform_native LANGUAGES CXX)

if(NOT ANDROID)
    message(FATAL_ERROR "Must use the Android NDK toolchain")
endif()

if(NOT "${ANDROID_ABI}" STREQUAL "arm64-v8a")
    message(FATAL_ERROR "Only arm64-v8a is supported; got '${ANDROID_ABI}'")
endif()

# LSPosed native entry: one shared library carries the Dart image plumbing and
# every native feature hook.  Features live under hooks/ and are dispatched by
# maxfreeform_native.cpp.
set(MAXFREEFORM_NATIVE_SOURCES
    maxfreeform_native.cpp
    hooks/add_freeform_shortcut_hook.cpp
    hooks/freeform_shortcut_icon_hook.cpp
)

set(NATIVE_HOOK_KIT_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../../../../native-hook-kit" CACHE PATH "NativeHookKit source directory")
add_subdirectory("${NATIVE_HOOK_KIT_DIR}" native_hook_kit)

set(MAXFREEFORM_NATIVE_TARGET maxfreeform_native)
set(MAXFREEFORM_NATIVE_EXPORTS exports-lsposed.map)

add_library(${MAXFREEFORM_NATIVE_TARGET} SHARED ${MAXFREEFORM_NATIVE_SOURCES})

target_include_directories(${MAXFREEFORM_NATIVE_TARGET} PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
)

target_compile_features(${MAXFREEFORM_NATIVE_TARGET} PRIVATE cxx_std_17)
target_compile_options(${MAXFREEFORM_NATIVE_TARGET} PRIVATE
    -fno-exceptions
    -fno-rtti
    -fno-threadsafe-statics
    -fvisibility=hidden
    -fvisibility-inlines-hidden
    -ffunction-sections
    -fdata-sections
    -mbranch-protection=standard
    -Wall
    -Wextra
    -Werror
)

target_link_options(${MAXFREEFORM_NATIVE_TARGET} PRIVATE
    "-Wl,--version-script=${CMAKE_CURRENT_SOURCE_DIR}/${MAXFREEFORM_NATIVE_EXPORTS}"
    -Wl,--gc-sections
    -Wl,--no-undefined
    -Wl,-z,relro
    -Wl,-z,now
    "-Wl,@${CMAKE_CURRENT_SOURCE_DIR}/linker-rpath.txt"
)

target_link_libraries(${MAXFREEFORM_NATIVE_TARGET} PRIVATE log dl z)
nhk_link_module(${MAXFREEFORM_NATIVE_TARGET})

set_target_properties(${MAXFREEFORM_NATIVE_TARGET} PROPERTIES
    CXX_EXTENSIONS OFF
    OUTPUT_NAME "${MAXFREEFORM_NATIVE_TARGET}"
)
