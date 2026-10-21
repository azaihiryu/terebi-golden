#ifndef SCE_CTRL_H
#define SCE_CTRL_H

#include <cstdint>

namespace Terebi {

enum SceCtrlButtons : uint32_t {
    SCE_CTRL_SELECT     = 0x00000001,
    SCE_CTRL_START      = 0x00000008,
    SCE_CTRL_UP         = 0x00000010,
    SCE_CTRL_RIGHT      = 0x00000020,
    SCE_CTRL_DOWN       = 0x00000040,
    SCE_CTRL_LEFT       = 0x00000080,
    SCE_CTRL_L1         = 0x00000100,
    SCE_CTRL_R1         = 0x00000200,
    SCE_CTRL_TRIANGLE   = 0x00001000,
    SCE_CTRL_CIRCLE     = 0x00002000,
    SCE_CTRL_CROSS      = 0x00004000,
    SCE_CTRL_SQUARE     = 0x00008000
};

struct SceCtrlData {
    uint64_t timeStamp = 0;
    uint32_t buttons = 0;
    uint8_t lx = 128; // Center
    uint8_t ly = 128; // Center
    uint8_t rx = 128; // Center
    uint8_t ry = 128; // Center
};

class InputManager {
public:
    static InputManager& getInstance();

    void updateInputState(int androidBitmask, float leftX, float leftY, float rightX, float rightY);
    SceCtrlData getCurrentCtrlData() const;

private:
    InputManager() = default;
    SceCtrlData m_ctrlData;
};

} // namespace Terebi

#endif // SCE_CTRL_H
