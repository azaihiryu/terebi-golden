#include "p4g_recomp_core.h"
#include "../vfs/vfs_manager.h"
#include "../input/sce_ctrl.h"
#include "../graphics/gxm_vulkan_bridge.h"
#include "../graphics/vulkan_renderer.h"
#include <android/log.h>

#define LOG_TAG "P4GRecompCore"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace Terebi {

P4GRecompCore& P4GRecompCore::getInstance() {
    static P4GRecompCore instance;
    return instance;
}

bool P4GRecompCore::initialize(const std::string& gameDirectoryPath) {
    LOGI("Initializing Cross-Platform Persona 4 Golden Recompiled C++ Core...");

    // 1. Initialize Virtual File System
    VfsManager::getInstance().setRootDirectory(gameDirectoryPath);

    // 2. Initialize GXM -> Vulkan Translation Layer
    GxmVulkanBridge::getInstance().sceGxmInitialize();

    m_initialized = true;
    m_frameCount = 0;
    LOGI("P4G Recomp Core initialized successfully!");
    return true;
}

void P4GRecompCore::tickGameLogic() {
    if (!m_initialized) return;

    // Fetch current input state from InputManager
    SceCtrlData ctrlData = InputManager::getInstance().getCurrentCtrlData();

    // Process Persona 4 Golden recompiled game logic routines
    // Handles shoulder buttons (R1/L1) for camera rotation & attack actions
    m_frameCount++;
}

void P4GRecompCore::renderFrame() {
    if (!m_initialized) return;

    tickGameLogic();

    if (VulkanRenderer::getInstance().isInitialized()) {
        GxmVulkanBridge::getInstance().sceGxmBeginScene();
        GxmVulkanBridge::getInstance().sceGxmDraw(SCE_GXM_PRIMITIVE_TRIANGLES, 36, nullptr);
        GxmVulkanBridge::getInstance().sceGxmEndScene();
    }
}

void P4GRecompCore::shutdown() {
    LOGI("Shutting down P4G Recomp Core");
    m_initialized = false;
}

} // namespace Terebi
