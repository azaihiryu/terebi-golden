#include "gxm_vulkan_bridge.h"
#include <android/log.h>

#define LOG_TAG "GxmVulkanBridge"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

namespace Terebi {

GxmVulkanBridge& GxmVulkanBridge::getInstance() {
    static GxmVulkanBridge instance;
    return instance;
}

int GxmVulkanBridge::sceGxmInitialize() {
    LOGI("GXM-Vulkan Translation Layer Initialized");
    return 0; // SCE_OK
}

void GxmVulkanBridge::sceGxmBeginScene() {
    VulkanRenderer::getInstance().beginFrame();
}

void GxmVulkanBridge::sceGxmSetViewport(float x, float y, float width, float height) {
    m_viewport.x = x;
    m_viewport.y = y;
    m_viewport.width = width;
    m_viewport.height = height;
}

void GxmVulkanBridge::sceGxmDraw(SceGxmPrimitiveType /* primType */, uint32_t /* vertexCount */, const void* /* vertexData */) {
    VkCommandBuffer cmd = VulkanRenderer::getInstance().getCurrentCommandBuffer();
    if (cmd != VK_NULL_HANDLE) {
        // Intercept recompiled GXM draw call and submit Vulkan command buffer commands
        VkViewport vp{m_viewport.x, m_viewport.y, m_viewport.width, m_viewport.height, 0.0f, 1.0f};
        vkCmdSetViewport(cmd, 0, 1, &vp);
    }
}

void GxmVulkanBridge::sceGxmEndScene() {
    VulkanRenderer::getInstance().endFrame();
}

} // namespace Terebi
