package com.example.melodist

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import java.awt.GraphicsEnvironment
import java.awt.Toolkit
import java.awt.Window

/**
 * Encapsula la lógica de maximizar/restaurar una ventana sin decoración (undecorated)
 * respetando la barra de tareas de Windows.
 *
 * [WindowPlacement.Maximized] en ventanas undecorated hace fullscreen real
 * y cubre la taskbar. En su lugar calculamos el área de trabajo disponible
 * usando [Toolkit.getScreenInsets], que excluye la taskbar y otros docks.
 */
class WindowMaximizer(
    private val windowState: WindowState,
    private val awtWindow: Window
) {
    private var savedSize: DpSize = windowState.size
    private var savedPosition: WindowPosition = windowState.position
    var isMaximized: Boolean = false
        private set

    fun toggleMaximize() {
        if (isMaximized) restore() else maximize()
    }

    fun maximize() {
        if (isMaximized) return
        // Guardar estado actual
        savedSize = windowState.size
        savedPosition = windowState.position

        try {
            val toolkit = Toolkit.getDefaultToolkit()
            val screenSize = toolkit.screenSize
            val gc = awtWindow.graphicsConfiguration
                ?: GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .defaultScreenDevice.defaultConfiguration
            val insets = toolkit.getScreenInsets(gc)

            val workX      = insets.left
            val workY      = insets.top
            val workWidth  = screenSize.width  - insets.left - insets.right
            val workHeight = screenSize.height - insets.top  - insets.bottom

            windowState.placement = WindowPlacement.Floating
            windowState.position  = WindowPosition(workX.dp, workY.dp)
            windowState.size      = DpSize(workWidth.dp, workHeight.dp)
        } catch (_: Exception) {
            // Fallback: Maximized nativo (puede cubrir taskbar en algunos entornos)
            windowState.placement = WindowPlacement.Maximized
        }

        isMaximized = true
    }

    fun restore() {
        if (!isMaximized) return
        windowState.placement = WindowPlacement.Floating
        windowState.size      = savedSize
        windowState.position  = savedPosition
        isMaximized = false
    }
}


