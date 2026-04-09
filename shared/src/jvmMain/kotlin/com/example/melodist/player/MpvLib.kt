package com.example.melodist.player

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import java.io.File

interface MpvLib : Library {
    fun mpv_create(): Pointer?
    fun mpv_initialize(handle: Pointer): Int
    fun mpv_command(handle: Pointer, args: Array<String?>): Int
    fun mpv_terminate_destroy(handle: Pointer)
    fun mpv_set_property_string(handle: Pointer, name: String, value: String): Int
    fun mpv_get_property_string(handle: Pointer, name: String): String?
    fun mpv_free(data: Pointer)

    companion object {
        val INSTANCE: MpvLib by lazy {
            val userDir = System.getProperty("user.dir")
            val possiblePaths = listOf(
                "$userDir\\mpv-resources\\windows",
                "$userDir\\resources",
                "C:\\Users\\WinterOS\\desktop\\melodist\\mpv-resources\\windows"
            )
            
            possiblePaths.forEach { path ->
                val file = File(path, "libmpv-2.dll")
                if (file.exists()) {
                    System.setProperty("jna.library.path", path)
                }
            }

            Native.load("libmpv-2", MpvLib::class.java)
        }
    }
}
