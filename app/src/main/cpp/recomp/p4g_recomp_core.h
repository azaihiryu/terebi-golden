#ifndef P4G_RECOMP_CORE_H
#define P4G_RECOMP_CORE_H

#include <string>
#include <memory>

namespace Terebi {

class P4GRecompCore {
public:
    static P4GRecompCore& getInstance();

    bool initialize(const std::string& gameDirectoryPath);
    void tickGameLogic();
    void renderFrame();
    void shutdown();

    bool isInitialized() const { return m_initialized; }

private:
    P4GRecompCore() = default;
    ~P4GRecompCore() = default;

    bool m_initialized = false;
    uint64_t m_frameCount = 0;
};

} // namespace Terebi

#endif // P4G_RECOMP_CORE_H
