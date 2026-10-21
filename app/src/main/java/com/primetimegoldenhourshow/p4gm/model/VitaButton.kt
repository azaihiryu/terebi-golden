package com.primetimegoldenhourshow.p4gm.model

enum class VitaButton(val displayName: String, val bitmask: Int) {
    UP("D-Pad Up", 0x0001),
    DOWN("D-Pad Down", 0x0002),
    LEFT("D-Pad Left", 0x0004),
    RIGHT("D-Pad Right", 0x0008),
    TRIANGLE("Triangle (Top)", 0x0010),
    CIRCLE("Circle (Right)", 0x0020),
    CROSS("Cross (Bottom)", 0x0040),
    SQUARE("Square (Left)", 0x0080),
    L1("L Trigger", 0x0100),
    R1("R Trigger", 0x0200),
    SELECT("Select Button", 0x0400),
    START("Start Button", 0x0800),
    PS("PS Button", 0x1000);

    companion object {
        fun fromDisplayName(name: String): VitaButton? {
            return values().find { it.displayName == name }
        }
    }
}
