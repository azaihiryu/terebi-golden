#ifndef VFS_MANAGER_H
#define VFS_MANAGER_H

#include <string>
#include <vector>
#include <unordered_map>
#include <fstream>
#include <memory>
#include <mutex>

namespace Terebi {

struct VfsFileHandle {
    int id = -1;
    std::string virtualPath;
    std::string resolvedRealPath;
    std::unique_ptr<std::ifstream> stream;
    size_t fileSize = 0;
};

class VfsManager {
public:
    static VfsManager& getInstance();

    void setRootDirectory(const std::string& absolutePath);
    std::string getRootDirectory() const;

    // Resolve Vita VFS paths ("host0:data/...", "ux0:data/...", "data/archive.pac")
    std::string resolveVirtualPath(const std::string& virtPath) const;

    // POSIX C++ Stream replacements for PS Vita sceIo* calls
    int sceIoOpen(const char* file, int flags, int mode);
    int sceIoRead(int fd, void* data, size_t size);
    long sceIoLseek(int fd, long offset, int whence);
    void sceIoClose(int fd);

    bool fileExists(const std::string& virtPath) const;

private:
    VfsManager() = default;
    ~VfsManager() = default;

    std::string m_rootDir;
    std::unordered_map<int, VfsFileHandle> m_openHandles;
    int m_nextFd = 1000;
    mutable std::mutex m_mutex;
};

} // namespace Terebi

#endif // VFS_MANAGER_H
