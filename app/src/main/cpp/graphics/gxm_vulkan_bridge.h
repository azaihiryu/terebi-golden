#ifndef GXM_VULKAN_BRIDGE_H
#define GXM_VULKAN_BRIDGE_H

#include "vulkan_renderer.h"

namespace Terebi {

// GXM API Types & Emulated Enums
enum SceGxmPrimitiveType {
    SCE_GXM_PRIMITIVE_TRIANGLES = 0,
    SCE_GXM_PRIMITIVE_TRIANGLE_STRIP = 1,
    SCE_GXM_PRIMITIVE_LINES = 2,
    SCE_GXM_PRIMITIVE_POINTS = 3
};

struct SceGxmViewport {
    float x;
    float y;
    float width;
    float height;
    float minDepth;
    float maxDepth;
};

class GxmVulkanBridge {
public:
    static GxmVulkanBridge& getInstance();

    int sceGxmInitialize();
    void sceGxmBeginScene();
    void sceGxmSetViewport(float x, float y, float width, float height);
    void sceGxmDraw(SceGxmPrimitiveType primType, uint32_t vertexCount, const void* vertexData);
    void sceGxmEndScene();

private:
    GxmVulkanBridge() = default;
    SceGxmViewport m_viewport{0.0f, 0.0f, 960.0f, 544.0f, 0.0f, 1.0f};
};

} // namespace Terebi

#endif // GXM_VULKAN_BRIDGE_H
