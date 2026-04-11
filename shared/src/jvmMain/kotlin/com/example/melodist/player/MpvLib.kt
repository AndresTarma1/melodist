package com.example.melodist.player

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import java.io.File
import java.util.logging.Logger

interface MpvLib : Library {
    fun mpv_create(): Pointer?
    fun mpv_initialize(handle: Pointer): Int
    fun mpv_command(handle: Pointer, args: Array<String?>): Int
    fun mpv_terminate_destroy(handle: Pointer)
    fun mpv_set_property_string(handle: Pointer, name: String, value: String): Int
    fun mpv_get_property_string(handle: Pointer, name: String): String?
    fun mpv_free(data: Pointer)

    companion object {
        private val log = Logger.getLogger("MpvLib")

        val INSTANCE: MpvLib by lazy {
            val userDir = File(System.getProperty("user.dir"))
            val rootDir = userDir.parentFile ?: userDir
            val possibleDirs = listOf(
                File(userDir, "app/app"),
                File(userDir, "app"),
                File(userDir, "mpv-resources/windows"),
                File(userDir, "mpv-resources"),
                File(userDir, "resources"),
                File(rootDir, "mpv-resources/windows"),
                File(rootDir, "mpv-resources"),
                File(rootDir, "resources")
            )

            val libraryDir = possibleDirs.firstOrNull { File(it, "libmpv-2.dll").exists() }
            if (libraryDir != null) {
                System.setProperty("jna.library.path", libraryDir.absolutePath)
                log.info("MpvLib: cargando libmpv desde ${libraryDir.absolutePath}")
            } else {
                log.warning(
                    "MpvLib: no se encontró libmpv-2.dll en: ${possibleDirs.joinToString { it.absolutePath }}"
                )
            }

            Native.load("libmpv-2", MpvLib::class.java)
        }
    }
}
