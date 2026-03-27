package com.example.melodist.utils

import com.sun.jna.Native
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.platform.win32.WinUser
import java.awt.Window

/**
 * Utilidades para interactuar con la API nativa de Windows a través de JNA.
 */
object WindowsUtils {

    /**
     * Maximiza la ventana especificada utilizando la API de Windows ShowWindow.
     * SW_SHOWMAXIMIZED = 3
     */
    fun maximizeWindow(window: Window) {
        try {
            // Obtener el puntero nativo de la ventana AWT
            val hwndPtr = Native.getWindowPointer(window)
            if (hwndPtr != null) {
                val hwnd = HWND(hwndPtr)
                // Usar ShowWindow con SW_SHOWMAXIMIZED
                User32.INSTANCE.ShowWindow(hwnd, WinUser.SW_SHOWMAXIMIZED)
            }
        } catch (e: Exception) {
            System.err.println("[DEBUG_LOG] Error al intentar maximizar con JNA: ${e.message}")
        }
    }
}
