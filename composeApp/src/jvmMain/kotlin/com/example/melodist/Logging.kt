package com.example.melodist

import com.example.melodist.data.AppDirs
import java.util.logging.FileHandler
import java.util.logging.Level
import java.util.logging.Logger
import java.util.logging.SimpleFormatter

/**
 * Redirige todos los logs de java.util.logging a ~/.melodist/melodist.log
 * para poder diagnosticar errores en la app instalada (sin consola).
 * Usa rotación: 2 archivos de máx 5 MB cada uno.
 */
fun setupFileLogging() {
    try {
        val logFile = AppDirs.dataRoot.resolve("melodist.log")
        val fileHandler = FileHandler(
            logFile.absolutePath,
            5 * 1024 * 1024, // 5 MB por archivo
            2,               // 2 archivos de rotación
            true             // append
        )
        fileHandler.formatter = SimpleFormatter()
        fileHandler.level = Level.INFO

        val rootLogger = Logger.getLogger("")
        rootLogger.addHandler(fileHandler)
        rootLogger.level = Level.INFO

        // Suprimir logs verbose de AWT/Swing (FINE/FINER/FINEST) que inundan el log
        arrayOf(
            "sun.awt", "java.awt", "javax.swing",
            "sun.lwawt", "sun.java2d", "sun.font"
        ).forEach {
            Logger.getLogger(it).level = Level.WARNING
        }
    } catch (_: Exception) {
        // Si no podemos crear el FileHandler, seguimos sin logging a archivo
    }
}

