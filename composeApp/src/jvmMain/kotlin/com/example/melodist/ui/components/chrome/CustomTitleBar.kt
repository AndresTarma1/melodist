package com.example.melodist.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState

/**
 * Custom title bar que reemplaza la barra nativa de Windows.
 * Combina con el MaterialTheme de la app.
 *
 * - Zona draggable para mover la ventana
 * - Botones Minimizar / Maximizar-Restaurar / Cerrar
 * - El botón de cerrar puede minimizar a tray (configurable)
 */
@Composable
fun FrameWindowScope.CustomTitleBar(
    title: String,
    windowState: WindowState,
    isMaximized: Boolean = false,
    onMinimize: () -> Unit,
    onMaximizeRestore: () -> Unit,
    onClose: () -> Unit,
) {
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainer
    val onSurface = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary

    // Si está pseudo-maximizado (Floating pero ocupando toda la pantalla de trabajo),
    // o si usa Maximized nativo, mostramos el icono de restaurar
    val isCurrentlyMaximized = isMaximized || windowState.placement == WindowPlacement.Maximized

    Surface(
        modifier = Modifier.fillMaxWidth().height(38.dp),
        color = surfaceColor,
        tonalElevation = 1.dp
    ) {
        // WindowDraggableArea para arrastrar la ventana.
        // El doble clic maximiza/restaura igual que la barra nativa de Windows.
        WindowDraggableArea(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    onClick = {},
                    onDoubleClick = onMaximizeRestore
                )
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono de la app
                Icon(
                    Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(Modifier.width(10.dp))

                // Título
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        letterSpacing = 0.3.sp
                    ),
                    color = onSurface,
                    maxLines = 1
                )

                Spacer(Modifier.weight(1f))

                // ─── Botones de ventana ─────────────────────
                TitleBarButton(
                    onClick = onMinimize,
                    icon = Icons.Filled.Minimize,
                    description = "Minimizar",
                    tint = onSurface.copy(alpha = 0.7f),
                    hoverColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                )

                TitleBarButton(
                    onClick = onMaximizeRestore,
                    icon = if (isCurrentlyMaximized)
                        Icons.Filled.FilterNone else Icons.Filled.CropSquare,
                    description = if (isCurrentlyMaximized)
                        "Restaurar" else "Maximizar",
                    tint = onSurface.copy(alpha = 0.7f),
                    hoverColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                )

                TitleBarButton(
                    onClick = onClose,
                    icon = Icons.Filled.Close,
                    description = "Cerrar",
                    tint = onSurface.copy(alpha = 0.7f),
                    hoverColor = MaterialTheme.colorScheme.error,
                    hoverTint = MaterialTheme.colorScheme.onError,
                )
            }
        }
    }
}

@Composable
private fun TitleBarButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    tint: Color,
    hoverColor: Color,
    hoverTint: Color? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val bgColor by animateColorAsState(
        if (isHovered) hoverColor else Color.Transparent,
        label = "titleBtnBg"
    )
    val iconTint by animateColorAsState(
        if (isHovered && hoverTint != null) hoverTint else tint,
        label = "titleBtnTint"
    )

    Box(
        modifier = Modifier
            .size(width = 46.dp, height = 38.dp)
            .background(bgColor)
            .hoverable(interactionSource)
            .pointerHoverIcon(PointerIcon.Default),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(38.dp),
            interactionSource = interactionSource
        ) {
            Icon(
                icon,
                contentDescription = description,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}




