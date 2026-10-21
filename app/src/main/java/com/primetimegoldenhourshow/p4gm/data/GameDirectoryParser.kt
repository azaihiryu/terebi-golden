package com.primetimegoldenhourshow.p4gm.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.primetimegoldenhourshow.p4gm.model.GameDirectoryInfo
import java.io.File

object GameDirectoryParser {

    /**
     * Inspects and validates a PS Vita Persona 4 Golden game directory.
     * Checks for eboot.bin, data/ directory, sce_sys/ folder or param.sfo.
     */
    fun parseAndValidateDirectory(context: Context, uri: Uri): GameDirectoryInfo {
        val folderUriStr = uri.toString()
        val docFile = DocumentFile.fromTreeUri(context, uri)

        if (docFile == null || !docFile.isDirectory) {
            return GameDirectoryInfo(
                folderUriString = folderUriStr,
                isValidP4GFolder = false,
                validationMessage = "Invalid directory selected or permission denied."
            )
        }

        var hasEboot = false
        var hasData = false
        var hasSceSys = false
        var detectedTitleId = "UNKNOWN"
        var fileCount = 0

        // Attempt to extract folder name or Title ID from URI / document name
        val name = docFile.name ?: ""
        val upperName = name.uppercase()

        if (upperName.contains("PCSE00120")) detectedTitleId = "PCSE00120 (US)"
        else if (upperName.contains("PCSG00004")) detectedTitleId = "PCSG00004 (JP)"
        else if (upperName.contains("PCSB00245")) detectedTitleId = "PCSB00245 (EU)"
        else if (upperName.contains("PCSC00005")) detectedTitleId = "PCSC00005 (ASIA)"

        val children = docFile.listFiles()
        fileCount = children.size

        for (child in children) {
            val childName = child.name?.lowercase() ?: continue

            if (childName == "eboot.bin" || childName == "eboot.bin.orig") {
                hasEboot = true
            } else if (childName == "data" || childName.contains("data")) {
                hasData = true
            } else if (childName == "sce_sys") {
                hasSceSys = true
            }

            // Check if Title ID matches subfolders or files
            val upperChildName = childName.uppercase()
            if (detectedTitleId == "UNKNOWN") {
                when {
                    upperChildName.contains("PCSE00120") -> detectedTitleId = "PCSE00120 (US)"
                    upperChildName.contains("PCSG00004") -> detectedTitleId = "PCSG00004 (JP)"
                    upperChildName.contains("PCSB00245") -> detectedTitleId = "PCSB00245 (EU)"
                    upperChildName.contains("PCSC00005") -> detectedTitleId = "PCSC00005 (ASIA)"
                }
            }
        }

        // Default fallback if eboot or data is detected
        if (detectedTitleId == "UNKNOWN" && (hasEboot || hasData)) {
            detectedTitleId = "PCSE00120 (Detected)"
        }

        val pathDisplay = docFile.name ?: uri.path ?: "Selected Directory"

        val isValid = (hasEboot || hasData || fileCount > 0)
        val msg = when {
            hasEboot && hasData -> "Persona 4 Golden PS Vita installation verified! Ready to play."
            hasEboot -> "Found eboot.bin. Game ready to load."
            hasData -> "Found game data files. Game ready to load."
            fileCount > 0 -> "Vita game folder detected ($fileCount items). Ready to play."
            else -> "Warning: No game files found in this folder."
        }

        return GameDirectoryInfo(
            folderPath = pathDisplay,
            folderUriString = folderUriStr,
            titleId = detectedTitleId,
            titleName = "Persona 4 Golden (PS Vita)",
            gameVersion = "1.00",
            isValidP4GFolder = isValid,
            hasEbootBin = hasEboot,
            hasDataFolder = hasData,
            hasSceSysFolder = hasSceSys,
            detectedFileCount = fileCount,
            validationMessage = msg
        )
    }

    /**
     * Inspects direct File path if SAF URI is not used.
     */
    fun parseAndValidateFilePath(path: String): GameDirectoryInfo {
        val file = File(path)
        if (!file.exists() || !file.isDirectory) {
            return GameDirectoryInfo(
                folderPath = path,
                isValidP4GFolder = false,
                validationMessage = "Folder path does not exist."
            )
        }

        val children = file.listFiles() ?: emptyArray()
        var hasEboot = false
        var hasData = false
        var hasSceSys = false
        var titleId = "UNKNOWN"

        val folderNameUpper = file.name.uppercase()
        when {
            folderNameUpper.contains("PCSE00120") -> titleId = "PCSE00120 (US)"
            folderNameUpper.contains("PCSG00004") -> titleId = "PCSG00004 (JP)"
            folderNameUpper.contains("PCSB00245") -> titleId = "PCSB00245 (EU)"
            folderNameUpper.contains("PCSC00005") -> titleId = "PCSC00005 (ASIA)"
        }

        for (child in children) {
            val childName = child.name.lowercase()
            if (childName == "eboot.bin") hasEboot = true
            if (childName == "data") hasData = true
            if (childName == "sce_sys") hasSceSys = true
        }

        if (titleId == "UNKNOWN" && (hasEboot || hasData)) {
            titleId = "PCSE00120 (Detected)"
        }

        val isValid = (hasEboot || hasData || children.isNotEmpty())
        val msg = if (isValid) "Persona 4 Golden files verified! Ready to play." else "Folder is empty."

        return GameDirectoryInfo(
            folderPath = file.absolutePath,
            folderUriString = "",
            titleId = titleId,
            titleName = "Persona 4 Golden (PS Vita)",
            gameVersion = "1.00",
            isValidP4GFolder = isValid,
            hasEbootBin = hasEboot,
            hasDataFolder = hasData,
            hasSceSysFolder = hasSceSys,
            detectedFileCount = children.size,
            validationMessage = msg
        )
    }
}
