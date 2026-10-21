#include <jni.h>
#include <string>
#include <android/log.h>
#include <android/native_window_jni.h>
#include <sstream>

#include "vfs/vfs_manager.h"
#include "input/sce_ctrl.h"
#include "graphics/vulkan_renderer.h"
#include "graphics/gxm_vulkan_bridge.h"
#include "recomp/p4g_recomp_core.h"

#define LOG_TAG "P4GNativeEngine"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

struct EngineSettings {
    float resScale = 2.0f;
    int upscalerFilter = 3; // 0: Bilinear, 1: Nearest, 2: FXAA, 3: FSR, 4: xBRZ
    int targetFps = 60;
    bool enableVsync = true;
    int aspectRatioType = 0; // 0: 16:9, 1: 16:10, 2: 21:9, 3: 4:3, 4: Stretch
    bool widescreenPatch = true;
    bool hudFix = true;
    bool fps60Patch = true;
};

static EngineSettings g_settings;
static std::string g_gameDirectoryPath = "";
static bool g_engineInitialized = false;
static uint64_t g_renderedFrames = 0;

extern "C" JNIEXPORT jstring JNICALL
Java_com_primetimegoldenhourshow_p4gm_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Terebi Golden Recomp Core (Vulkan + GXM Bridge) Ready";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_primetimegoldenhourshow_p4gm_native_P4GNativeEngine_initEngine(
        JNIEnv* env,
        jobject /* this */,
        jstring gameDirectoryPath) {

    const char* pathStr = env->GetStringUTFChars(gameDirectoryPath, nullptr);
    if (pathStr != nullptr) {
        g_gameDirectoryPath = std::string(pathStr);
        env->ReleaseStringUTFChars(gameDirectoryPath, pathStr);
    }

    LOGI("Initializing P4G Native Engine with SAF path: %s", g_gameDirectoryPath.c_str());

    // Initialize VFS and Recomp Core
    Terebi::P4GRecompCore::getInstance().initialize(g_gameDirectoryPath);

    g_engineInitialized = true;
    g_renderedFrames = 0;
    return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_primetimegoldenhourshow_p4gm_native_P4GNativeEngine_setSurface(
        JNIEnv* env,
        jobject /* this */,
        jobject surface) {

    if (!surface) {
        LOGE("setSurface called with null Surface object");
        return JNI_FALSE;
    }

    ANativeWindow* window = ANativeWindow_fromSurface(env, surface);
    if (!window) {
        LOGE("Failed to extract ANativeWindow from Surface");
        return JNI_FALSE;
    }

    LOGI("Initializing Vulkan renderer via ANativeWindow surface");
    bool success = Terebi::VulkanRenderer::getInstance().initVulkan(window);
    return success ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_primetimegoldenhourshow_p4gm_native_P4GNativeEngine_destroySurface(
        JNIEnv* /* env */,
        jobject /* this */) {

    LOGI("Destroying Vulkan Surface");
    Terebi::VulkanRenderer::getInstance().cleanup();
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_primetimegoldenhourshow_p4gm_native_P4GNativeEngine_configureGraphics(
        JNIEnv* /* env */,
        jobject /* this */,
        jfloat resScale,
        jint upscalerFilterType,
        jint targetFps,
        jboolean enableVsync,
        jint aspectRatioType,
        jboolean enableWidescreenPatch,
        jboolean enableHudFix,
        jboolean enable60FpsPatch) {

    g_settings.resScale = resScale;
    g_settings.upscalerFilter = upscalerFilterType;
    g_settings.targetFps = targetFps;
    g_settings.enableVsync = enableVsync;
    g_settings.aspectRatioType = aspectRatioType;
    g_settings.widescreenPatch = enableWidescreenPatch;
    g_settings.hudFix = enableHudFix;
    g_settings.fps60Patch = enable60FpsPatch;

    LOGI("Graphics Configured -> Res: %.2fx, Filter: %d, FPS: %d, VSync: %d, FOV Patch: %d",
         resScale, upscalerFilterType, targetFps, enableVsync, enableWidescreenPatch);

    return JNI_TRUE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_primetimegoldenhourshow_p4gm_native_P4GNativeEngine_sendInputState(
        JNIEnv* /* env */,
        jobject /* this */,
        jint buttonBitmask,
        jfloat leftStickX,
        jfloat leftStickY,
        jfloat rightStickX,
        jfloat rightStickY) {

    // Wire input bitmask to PS Vita's SceCtrlData struct
    Terebi::InputManager::getInstance().updateInputState(
        buttonBitmask, leftStickX, leftStickY, rightStickX, rightStickY
    );
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_primetimegoldenhourshow_p4gm_native_P4GNativeEngine_getEngineStatus(
        JNIEnv* env,
        jobject /* this */) {

    std::stringstream ss;
    ss << "P4G Core: " << (g_engineInitialized ? "ACTIVE" : "STANDBY") << "\n";
    ss << "Vulkan Backend: " << (Terebi::VulkanRenderer::getInstance().isInitialized() ? "ONLINE" : "OFFLINE") << "\n";
    ss << "Directory: " << (g_gameDirectoryPath.empty() ? "None" : g_gameDirectoryPath) << "\n";
    ss << "Render Scale: " << g_settings.resScale << "x | FPS Limit: " << g_settings.targetFps << "\n";
    ss << "Widescreen FOV Patch: " << (g_settings.widescreenPatch ? "ENABLED" : "DISABLED") << "\n";
    ss << "Rendered Frames: " << g_renderedFrames;

    return env->NewStringUTF(ss.str().c_str());
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_primetimegoldenhourshow_p4gm_native_P4GNativeEngine_renderFrame(
        JNIEnv* /* env */,
        jobject /* this */) {

    if (!g_engineInitialized) return JNI_FALSE;

    Terebi::P4GRecompCore::getInstance().renderFrame();
    g_renderedFrames++;
    return JNI_TRUE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_primetimegoldenhourshow_p4gm_native_P4GNativeEngine_stopEngine(
        JNIEnv* /* env */,
        jobject /* this */) {

    LOGI("Stopping P4G Native Engine");
    Terebi::P4GRecompCore::getInstance().shutdown();
    Terebi::VulkanRenderer::getInstance().cleanup();
    g_engineInitialized = false;
}
