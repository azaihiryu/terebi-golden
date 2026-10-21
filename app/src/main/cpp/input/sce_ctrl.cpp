#include "sce_ctrl.h"
#include <algorithm>

namespace Terebi {

InputManager& InputManager::getInstance() {
    static InputManager instance;
    return instance;
}

static uint8_t floatToStickByte(float val) {
    // Input range -1.0 .. +1.0 -> 0 .. 255 (Center 128)
    float clamped = std::max(-1.0f, std::min(1.0f, val));
    return static_cast<uint8_t>(128.0f + (clamped * 127.0f));
}

void InputManager::updateInputState(int androidBitmask, float leftX, float leftY, float rightX, float rightY) {
    uint32_t vitaButtons = 0;

    // Bitmask mapping matching VitaButton enum in Kotlin:
    // UP (0x0001), DOWN (0x0002), LEFT (0x0004), RIGHT (0x0008)
    // TRIANGLE (0x0010), CIRCLE (0x0020), CROSS (0x0040), SQUARE (0x0080)
    // L1 (0x0100), R1 (0x0200), SELECT (0x0400), START (0x0800)

    if (androidBitmask & 0x0001) vitaButtons |= SCE_CTRL_UP;
    if (androidBitmask & 0x0002) vitaButtons |= SCE_CTRL_DOWN;
    if (androidBitmask & 0x0004) vitaButtons |= SCE_CTRL_LEFT;
    if (androidBitmask & 0x0008) vitaButtons |= SCE_CTRL_RIGHT;

    if (androidBitmask & 0x0010) vitaButtons |= SCE_CTRL_TRIANGLE;
    if (androidBitmask & 0x0020) vitaButtons |= SCE_CTRL_CIRCLE;
    if (androidBitmask & 0x0040) vitaButtons |= SCE_CTRL_CROSS;
    if (androidBitmask & 0x0080) vitaButtons |= SCE_CTRL_SQUARE;

    // Map shoulder buttons (L1/R1) for Persona 4 Golden camera & attack actions
    if (androidBitmask & 0x0100) vitaButtons |= SCE_CTRL_L1;
    if (androidBitmask & 0x0200) vitaButtons |= SCE_CTRL_R1;

    if (androidBitmask & 0x0400) vitaButtons |= SCE_CTRL_SELECT;
    if (androidBitmask & 0x0800) vitaButtons |= SCE_CTRL_START;

    m_ctrlData.buttons = vitaButtons;
    m_ctrlData.lx = floatToStickByte(leftX);
    m_ctrlData.ly = floatToStickByte(leftY);
    m_ctrlData.rx = floatToStickByte(rightX);
    m_ctrlData.ry = floatToStickByte(rightY);
}

SceCtrlData InputManager::getCurrentCtrlData() const {
    return m_ctrlData;
}

} // namespace Terebi
