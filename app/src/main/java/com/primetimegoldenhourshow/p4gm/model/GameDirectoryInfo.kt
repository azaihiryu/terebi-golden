package com.primetimegoldenhourshow.p4gm.model

data class GameDirectoryInfo(
    val folderPath: String = "",
    val folderUriString: String = "",
    val titleId: String = "UNKNOWN",
    val titleName: String = "Persona 4 Golden",
    val gameVersion: String = "1.00",
    val isValidP4GFolder: Boolean = false,
    val hasEbootBin: Boolean = false,
    val hasDataFolder: Boolean = false,
    val hasSceSysFolder: Boolean = false,
    val detectedFileCount: Int = 0,
    val validationMessage: String = "No directory selected."
)
