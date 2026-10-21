# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)

# Declares the project name.
project("p4gm")

# Set C++ standard to C++17
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Source files
set(SOURCE_FILES
    native-lib.cpp
    vfs/vfs_manager.cpp
    input/sce_ctrl.cpp
    graphics/vulkan_renderer.cpp
    graphics/gxm_vulkan_bridge.cpp
    recomp/p4g_recomp_core.cpp
)

# Creates and names a library target
add_library(${CMAKE_PROJECT_NAME} SHARED ${SOURCE_FILES})

# Include directories
target_include_directories(${CMAKE_PROJECT_NAME} PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${CMAKE_CURRENT_SOURCE_DIR}/vfs
    ${CMAKE_CURRENT_SOURCE_DIR}/input
    ${CMAKE_CURRENT_SOURCE_DIR}/graphics
    ${CMAKE_CURRENT_SOURCE_DIR}/recomp
)

# Link NDK system libraries: android (ANativeWindow), log, vulkan
target_link_libraries(${CMAKE_PROJECT_NAME}
    android
    log
    vulkan
)
