package com.example.melodist.ui.components

import androidx.compose.runtime.*
import androidx.compose.ui.window.FrameWindowScope
import java.awt.Cursor
import java.awt.Point
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import javax.swing.JComponent
import javax.swing.SwingUtilities

/**
 * Habilita el resize manual en los bordes de una ventana sin decoración (undecorated).
 * Usa un JComponent invisible superpuesto que solo intercepta eventos en la franja
 * de 5 px de los bordes, dejando pasar el resto a Compose.
 */
@Composable
fun FrameWindowScope.WindowResizeBorder(isMaximized: Boolean = false) {
    val borderSize = 5

    DisposableEffect(window) {
        val frame = window

        // Overlay transparente que solo detecta bordes
        val overlay = object : JComponent() {
            init {
                isOpaque = false
                // Solo consume eventos si el cursor está en la franja de borde
            }

            override fun contains(x: Int, y: Int): Boolean {
                val w = width
                val h = height
                return x < borderSize || x > w - borderSize ||
                       y < borderSize || y > h - borderSize
            }
        }

        var dragStartPoint: Point? = null
        var dragStartBounds: java.awt.Rectangle? = null
        var resizeDirection = Cursor.DEFAULT_CURSOR

        fun getResizeDirection(x: Int, y: Int, w: Int, h: Int): Int = when {
            x < borderSize && y < borderSize -> Cursor.NW_RESIZE_CURSOR
            x > w - borderSize && y < borderSize -> Cursor.NE_RESIZE_CURSOR
            x < borderSize && y > h - borderSize -> Cursor.SW_RESIZE_CURSOR
            x > w - borderSize && y > h - borderSize -> Cursor.SE_RESIZE_CURSOR
            x < borderSize -> Cursor.W_RESIZE_CURSOR
            x > w - borderSize -> Cursor.E_RESIZE_CURSOR
            y < borderSize -> Cursor.N_RESIZE_CURSOR
            y > h - borderSize -> Cursor.S_RESIZE_CURSOR
            else -> Cursor.DEFAULT_CURSOR
        }

        overlay.addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseMoved(e: MouseEvent) {
                if (isMaximized) { overlay.cursor = Cursor.getDefaultCursor(); return }
                val dir = getResizeDirection(e.x, e.y, overlay.width, overlay.height)
                overlay.cursor = Cursor.getPredefinedCursor(dir)
            }

            override fun mouseDragged(e: MouseEvent) {
                if (isMaximized) return
                val start = dragStartPoint ?: return
                val bounds = dragStartBounds ?: return
                val current = e.locationOnScreen
                val dx = current.x - start.x
                val dy = current.y - start.y

                val minW = frame.minimumSize.width
                val minH = frame.minimumSize.height
                val nb = java.awt.Rectangle(bounds)

                when (resizeDirection) {
                    Cursor.E_RESIZE_CURSOR -> nb.width = maxOf(minW, bounds.width + dx)
                    Cursor.S_RESIZE_CURSOR -> nb.height = maxOf(minH, bounds.height + dy)
                    Cursor.W_RESIZE_CURSOR -> {
                        val nw = maxOf(minW, bounds.width - dx)
                        nb.x = bounds.x + bounds.width - nw; nb.width = nw
                    }
                    Cursor.N_RESIZE_CURSOR -> {
                        val nh = maxOf(minH, bounds.height - dy)
                        nb.y = bounds.y + bounds.height - nh; nb.height = nh
                    }
                    Cursor.SE_RESIZE_CURSOR -> {
                        nb.width = maxOf(minW, bounds.width + dx)
                        nb.height = maxOf(minH, bounds.height + dy)
                    }
                    Cursor.NE_RESIZE_CURSOR -> {
                        nb.width = maxOf(minW, bounds.width + dx)
                        val nh = maxOf(minH, bounds.height - dy)
                        nb.y = bounds.y + bounds.height - nh; nb.height = nh
                    }
                    Cursor.SW_RESIZE_CURSOR -> {
                        val nw = maxOf(minW, bounds.width - dx)
                        nb.x = bounds.x + bounds.width - nw; nb.width = nw
                        nb.height = maxOf(minH, bounds.height + dy)
                    }
                    Cursor.NW_RESIZE_CURSOR -> {
                        val nw = maxOf(minW, bounds.width - dx)
                        val nh = maxOf(minH, bounds.height - dy)
                        nb.x = bounds.x + bounds.width - nw; nb.width = nw
                        nb.y = bounds.y + bounds.height - nh; nb.height = nh
                    }
                }
                frame.bounds = nb
            }
        })

        overlay.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                if (isMaximized) return
                resizeDirection = getResizeDirection(e.x, e.y, overlay.width, overlay.height)
                if (resizeDirection != Cursor.DEFAULT_CURSOR) {
                    dragStartPoint = e.locationOnScreen
                    dragStartBounds = frame.bounds
                }
            }

            override fun mouseReleased(e: MouseEvent) {
                dragStartPoint = null
                dragStartBounds = null
                resizeDirection = Cursor.DEFAULT_CURSOR
            }
        })

        // Añadir el overlay al glassPane del JFrame
        SwingUtilities.invokeLater {
            val glass = frame.glassPane as? JComponent ?: return@invokeLater
            glass.layout = java.awt.BorderLayout()
            glass.add(overlay, java.awt.BorderLayout.CENTER)
            glass.isVisible = true
        }

        onDispose {
            SwingUtilities.invokeLater {
                val glass = frame.glassPane as? JComponent ?: return@invokeLater
                glass.remove(overlay)
                glass.isVisible = false
            }
        }
    }
}


