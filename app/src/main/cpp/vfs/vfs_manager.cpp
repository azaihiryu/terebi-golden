#include "vfs_manager.h"
#include <android/log.h>
#include <filesystem>

#define LOG_TAG "TerebiVFS"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace Terebi {

VfsManager& VfsManager::getInstance() {
    static VfsManager instance;
    return instance;
}

void VfsManager::setRootDirectory(const std::string& absolutePath) {
    std::lock_guard<std::mutex> lock(m_mutex);
    m_rootDir = absolutePath;
    if (!m_rootDir.empty() && m_rootDir.back() != '/') {
        m_rootDir += "/";
    }
    LOGI("VFS Root Directory set to: %s", m_rootDir.c_str());
}

std::string VfsManager::getRootDirectory() const {
    std::lock_guard<std::mutex> lock(m_mutex);
    return m_rootDir;
}

std::string VfsManager::resolveVirtualPath(const std::string& virtPath) const {
    std::string clean = virtPath;

    // Remove Vita mount prefixes if present ("host0:", "ux0:", "app0:")
    if (clean.find("host0:") == 0) clean = clean.substr(6);
    else if (clean.find("ux0:") == 0) clean = clean.substr(4);
    else if (clean.find("app0:") == 0) clean = clean.substr(5);

    while (!clean.empty() && (clean.front() == '/' || clean.front() == '\\')) {
        clean.erase(clean.begin());
    }

    // Convert Windows backslashes to POSIX forward slashes
    for (char& c : clean) {
        if (c == '\\') c = '/';
    }

    std::string realPath = m_rootDir + clean;
    return realPath;
}

bool VfsManager::fileExists(const std::string& virtPath) const {
    std::string realPath = resolveVirtualPath(virtPath);
    return std::filesystem::exists(realPath);
}

int VfsManager::sceIoOpen(const char* file, int /* flags */, int /* mode */) {
    if (!file) return -1;

    std::lock_guard<std::mutex> lock(m_mutex);
    std::string virtPath(file);
    std::string realPath = resolveVirtualPath(virtPath);

    auto stream = std::make_unique<std::ifstream>(realPath, std::ios::binary | std::ios::ate);
    if (!stream->is_open()) {
        LOGE("sceIoOpen failed for virtual path: %s (real: %s)", file, realPath.c_str());
        return -1;
    }

    size_t fileSize = stream->tellg();
    stream->seekg(0, std::ios::beg);

    int fd = m_nextFd++;
    VfsFileHandle handle;
    handle.id = fd;
    handle.virtualPath = virtPath;
    handle.resolvedRealPath = realPath;
    handle.stream = std::move(stream);
    handle.fileSize = fileSize;

    m_openHandles[fd] = std::move(handle);
    LOGI("sceIoOpen success: fd %d for %s (%zu bytes)", fd, virtPath.c_str(), fileSize);
    return fd;
}

int VfsManager::sceIoRead(int fd, void* data, size_t size) {
    if (!data || size == 0) return 0;

    std::lock_guard<std::mutex> lock(m_mutex);
    auto it = m_openHandles.find(fd);
    if (it == m_openHandles.end() || !it->second.stream) {
        LOGE("sceIoRead invalid fd: %d", fd);
        return -1;
    }

    auto& stream = it->second.stream;
    stream->read(reinterpret_cast<char*>(data), size);
    std::streamsize bytesRead = stream->gcount();
    return static_cast<int>(bytesRead);
}

long VfsManager::sceIoLseek(int fd, long offset, int whence) {
    std::lock_guard<std::mutex> lock(m_mutex);
    auto it = m_openHandles.find(fd);
    if (it == m_openHandles.end() || !it->second.stream) {
        return -1;
    }

    auto& stream = it->second.stream;
    std::ios_base::seekdir dir = std::ios::beg;
    if (whence == 1) dir = std::ios::cur;
    else if (whence == 2) dir = std::ios::end;

    stream->seekg(offset, dir);
    return static_cast<long>(stream->tellg());
}

void VfsManager::sceIoClose(int fd) {
    std::lock_guard<std::mutex> lock(m_mutex);
    auto it = m_openHandles.find(fd);
    if (it != m_openHandles.end()) {
        if (it->second.stream && it->second.stream->is_open()) {
            it->second.stream->close();
        }
        m_openHandles.erase(it);
        LOGI("sceIoClose fd: %d", fd);
    }
}

} // namespace Terebi
