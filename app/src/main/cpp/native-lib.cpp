#include <jni.h>
#include <string>
#include <android/log.h>
#include <sstream>

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

struct InputState {
    int buttonBitmask = 0;
    float leftX = 0.0f;
    float leftY = 0.0f;
    float rightX = 0.0f;
    float rightY = 0.0f;
};

static EngineSettings g_settings;
static InputState g_inputState;
static std::string g_gameDirectoryPath = "";
static bool g_engineInitialized = false;
static uint64_t g_renderedFrames = 0;

extern "C" JNIEXPORT jstring JNICALL
Java_com_primetimegoldenhourshow_p4gm_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Terebi Golden Engine Ready";
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

    LOGI("Initializing P4G Recomp Engine with directory: %s", g_gameDirectoryPath.c_str());
    g_engineInitialized = true;
    g_renderedFrames = 0;
    return JNI_TRUE;
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

    LOGI("Graphics Configured -> Res: %.2fx, Upscaler: %d, Target FPS: %d, VSync: %d, Widescreen: %d",
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

    g_inputState.buttonBitmask = buttonBitmask;
    g_inputState.leftX = leftStickX;
    g_inputState.leftY = leftStickY;
    g_inputState.rightX = rightStickX;
    g_inputState.rightY = rightStickY;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_primetimegoldenhourshow_p4gm_native_P4GNativeEngine_getEngineStatus(
        JNIEnv* env,
        jobject /* this */) {

    std::stringstream ss;
    ss << "P4G Native Core: " << (g_engineInitialized ? "ACTIVE" : "STANDBY") << "\n";
    ss << "Directory: " << (g_gameDirectoryPath.empty() ? "None" : g_gameDirectoryPath) << "\n";
    ss << "Render Scale: " << g_settings.resScale << "x | FPS Limit: " << g_settings.targetFps << "\n";
    ss << "Widescreen FOV Patch: " << (g_settings.widescreenPatch ? "ENABLED" : "DISABLED") << "\n";
    ss << "Total Rendered Frames: " << g_renderedFrames;

    return env->NewStringUTF(ss.str().c_str());
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_primetimegoldenhourshow_p4gm_native_P4GNativeEngine_renderFrame(
        JNIEnv* /* env */,
        jobject /* this */) {

    if (!g_engineInitialized) return JNI_FALSE;

    g_renderedFrames++;
    return JNI_TRUE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_primetimegoldenhourshow_p4gm_native_P4GNativeEngine_stopEngine(
        JNIEnv* /* env */,
        jobject /* this */) {

    LOGI("Stopping P4G Native Core Engine");
    g_engineInitialized = false;
}
