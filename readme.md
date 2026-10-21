### 📱 Installation & Setup Workflow
1. **Install the APK**
2. **Load PS Vita P4G in Directory Format**
    - Use the **Load P4G Directory Folder** picker on the Dashboard.
    - Point to your decrypted PS Vita Persona 4 Golden game directory (containing `eboot.bin`, `data/`, or `sce_sys/` with Title IDs such as `PCSE00120` [US], `PCSG00004` [JP], `PCSB00245` [EU], or `PCSC00005` [Asia]).
    - The directory parser verifies file integrity and saves persistent storage permissions.
3. **Ready to Play**
    - Once validated, the dashboard displays **"● Ready to Play"** along with Title ID badges and verified file counts.
    - Press **▶ PLAY GAME** to launch the game execution surface.

---

### ⚙️ Launcher & Engine Features Implemented

#### 1. 🎨 **Graphic Upscaler**
- **Internal Resolution Scale**:
    - `1.0x` (540p Vita Native)
    - `1.25x` (675p)
    - `1.5x` (810p HD)
    - `2.0x` (1080p FHD - Default)
    - `3.0x` (1620p 2K)
    - `4.0x` (2160p 4K)
- **Upscaling & Texture Filters**:
    - Bilinear, Nearest Neighbor, FXAA Anti-Aliasing, **AMD FSR (FidelityFX Super Resolution)**, xBRZ HQ.

#### 2. ⚡ **FPS & Performance Setting**
- **Target Framerate Options**: `30 FPS` (Vita Original), `60 FPS` (Smooth High), `120 FPS` (Ultra), and `Unlocked / Max FPS`.
- **VSync Toggle**: Tear-free synchronization option.

#### 3. 🎮 **Configure Controls**
- **On-Screen Touch Overlay**:
    - Custom Vita control overlay rendering D-Pad, Diamond Action Buttons (`△`, `○`, `✕`, `□`), `L1`/`R1` Triggers, and `Select`/`Start`.
    - **Button Opacity Slider** (`0% - 100%`).
    - **Button Scale / Size Slider** (`50% - 150%`).
    - **Haptic Vibration Feedback** toggle.
- **External Controller Mapping**:
    - Auto-detection for connected Bluetooth/USB gamepads (Xbox, PlayStation DualSense/DualShock, SteelSeries, Razer Kishi, etc.).
    - Interactive **Key Mapping Dialog** to bind physical gamepad buttons to Vita inputs (`UP`, `DOWN`, `LEFT`, `RIGHT`, `TRIANGLE`, `CIRCLE`, `CROSS`, `SQUARE`, `L`, `R`, `SELECT`, `START`).

#### 4. 🖥️ **Widescreen Stretch & Patches**
- **Aspect Ratio Options**: `16:9 Standard`, `16:10 Tablet`, `21:9 Ultrawide`, `4:3 Classic`, `Stretch to Fill`.
- **Widescreen Anamorphic FOV Camera Patch**: Adjusts 3D projection camera frustum to prevent 3D stretching on 16:9 / 21:9 screens.
- **UI / HUD Centering Fix**: Keeps 2D HUD elements and dialogue text boxes properly centered and unstretched.
- **60 FPS Logic & Motion Patch**: Prevents game logic speedup or camera jitter when playing at 60 FPS or above.

---

### 📁 Architecture & Code Summary

| Component | File Path | Purpose |
| :--- | :--- | :--- |
| **Native Core Engine** | [`native-lib.cpp`](file:///C:/Users/AmbaRagnamok/AndroidStudioProjects/TerebiGolden/app/src/main/cpp/native-lib.cpp) | C++ JNI bridge handling graphics pipeline, resolution scaling, frame pacing, and controller input buffers. |
| **JNI Native Bridge** | [`P4GNativeEngine.kt`](file:///C:/Users/AmbaRagnamok/AndroidStudioProjects/TerebiGolden/app/src/main/java/com/primetimegoldenhourshow/p4gm/native/P4GNativeEngine.kt) | Kotlin singleton passing render settings, widescreen patches, and input bitmasks to native C++. |
| **Directory Parser** | [`GameDirectoryParser.kt`](file:///C:/Users/AmbaRagnamok/AndroidStudioProjects/TerebiGolden/app/src/main/java/com/primetimegoldenhourshow/p4gm/data/GameDirectoryParser.kt) | SAF & file inspector checking Vita game folders (`eboot.bin`, `data/`, `sce_sys/param.sfo`) and extracting Title IDs. |
| **Settings Storage** | [`SettingsRepository.kt`](file:///C:/Users/AmbaRagnamok/AndroidStudioProjects/TerebiGolden/app/src/main/java/com/primetimegoldenhourshow/p4gm/data/SettingsRepository.kt) | Manages persistent app configurations, graphics profile, target FPS, and gamepad keybindings. |
| **Touch Overlay View** | [`TouchOverlayView.kt`](file:///C:/Users/AmbaRagnamok/AndroidStudioProjects/TerebiGolden/app/src/main/java/com/primetimegoldenhourshow/p4gm/ui/TouchOverlayView.kt) | Custom touch view rendering Persona 4 Golden translucent touch buttons, analog sticks, and haptic feedback. |
| **Dashboard Fragment** | [`DashboardFragment.kt`](file:///C:/Users/AmbaRagnamok/AndroidStudioProjects/TerebiGolden/app/src/main/java/com/primetimegoldenhourshow/p4gm/ui/DashboardFragment.kt) | Folder loader, game status card, installation guide, and quick start launcher. |
| **Graphics Fragment** | [`GraphicsFragment.kt`](file:///C:/Users/AmbaRagnamok/AndroidStudioProjects/TerebiGolden/app/src/main/java/com/primetimegoldenhourshow/p4gm/ui/GraphicsFragment.kt) | Configuration UI for Graphic Upscaler, Target FPS, VSync, Aspect Ratio, and Widescreen Patches. |
| **Controls Fragment** | [`ControlsFragment.kt`](file:///C:/Users/AmbaRagnamok/AndroidStudioProjects/TerebiGolden/app/src/main/java/com/primetimegoldenhourshow/p4gm/ui/ControlsFragment.kt) | Touch overlay customization, gamepad status detector, and controller key mapping. |
| **Game Files Fragment** | [`GameFilesFragment.kt`](file:///C:/Users/AmbaRagnamok/AndroidStudioProjects/TerebiGolden/app/src/main/java/com/primetimegoldenhourshow/p4gm/ui/GameFilesFragment.kt) | File integrity scanner and mod/patch enhancement status. |
| **Game Execution Activity**| [`GameActivity.kt`](file:///C:/Users/AmbaRagnamok/AndroidStudioProjects/TerebiGolden/app/src/main/java/com/primetimegoldenhourshow/p4gm/ui/GameActivity.kt) | Immersive landscape fullscreen surface hosting `SurfaceView`, touch overlay, FPS counter, and external controller listeners. |