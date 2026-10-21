#ifndef VULKAN_RENDERER_H
#define VULKAN_RENDERER_H

#include <vulkan/vulkan.h>
#include <android/native_window.h>
#include <vector>

namespace Terebi {

class VulkanRenderer {
public:
    static VulkanRenderer& getInstance();

    bool initVulkan(ANativeWindow* window);
    void cleanup();

    void beginFrame();
    void endFrame();

    bool isInitialized() const { return m_initialized; }
    VkDevice getDevice() const { return m_device; }
    VkRenderPass getRenderPass() const { return m_renderPass; }
    VkCommandBuffer getCurrentCommandBuffer() const;

    void setViewportAndScissor(uint32_t width, uint32_t height);

private:
    VulkanRenderer() = default;
    ~VulkanRenderer() = default;

    bool createInstance();
    bool selectPhysicalDevice();
    bool createLogicalDevice();
    bool createSurface(ANativeWindow* window);
    bool createSwapchain();
    bool createRenderPass();
    bool createFramebuffers();
    bool createCommandBuffers();

    bool m_initialized = false;
    ANativeWindow* m_nativeWindow = nullptr;

    VkInstance m_instance = VK_NULL_HANDLE;
    VkPhysicalDevice m_physicalDevice = VK_NULL_HANDLE;
    VkDevice m_device = VK_NULL_HANDLE;
    VkQueue m_graphicsQueue = VK_NULL_HANDLE;
    VkQueue m_presentQueue = VK_NULL_HANDLE;
    uint32_t m_graphicsQueueFamily = 0;
    uint32_t m_presentQueueFamily = 0;

    VkSurfaceKHR m_surface = VK_NULL_HANDLE;
    VkSwapchainKHR m_swapchain = VK_NULL_HANDLE;
    VkFormat m_swapchainFormat = VK_FORMAT_R8G8B8A8_UNORM;
    VkExtent2D m_swapchainExtent = {0, 0};

    std::vector<VkImage> m_swapchainImages;
    std::vector<VkImageView> m_swapchainImageViews;
    std::vector<VkFramebuffer> m_framebuffers;

    VkRenderPass m_renderPass = VK_NULL_HANDLE;
    VkCommandPool m_commandPool = VK_NULL_HANDLE;
    std::vector<VkCommandBuffer> m_commandBuffers;

    VkSemaphore m_imageAvailableSemaphore = VK_NULL_HANDLE;
    VkSemaphore m_renderFinishedSemaphore = VK_NULL_HANDLE;
    VkFence m_inFlightFence = VK_NULL_HANDLE;

    uint32_t m_currentImageIndex = 0;
};

} // namespace Terebi

#endif // VULKAN_RENDERER_H
