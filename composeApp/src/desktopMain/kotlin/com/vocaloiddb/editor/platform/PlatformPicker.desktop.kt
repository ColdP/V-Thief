package btm.m.vthief.platform

import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser

actual object PlatformPicker {
    actual fun chooseDirectory(initial: String?): String? {
        val chooser = JFileChooser(initial?.let(::File))
        chooser.dialogTitle = "选择声库工作区"
        chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        return if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) chooser.selectedFile.absolutePath else null
    }

    actual fun chooseFile(title: String, extensions: List<String>): String? {
        val chooser = JFileChooser()
        chooser.dialogTitle = title
        chooser.fileFilter = javax.swing.filechooser.FileNameExtensionFilter(extensions.joinToString("/").uppercase(), *extensions.toTypedArray())
        return if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) chooser.selectedFile.absolutePath else null
    }

    actual fun chooseSaveFile(title: String, suggestedName: String, extension: String): String? {
        val chooser = JFileChooser()
        chooser.dialogTitle = title
        chooser.selectedFile = File(suggestedName)
        chooser.fileFilter = javax.swing.filechooser.FileNameExtensionFilter(extension.uppercase(), extension)
        if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) return null
        val selected = chooser.selectedFile
        return if (selected.extension.equals(extension, true)) selected.absolutePath else "$selected.$extension"
    }
}

