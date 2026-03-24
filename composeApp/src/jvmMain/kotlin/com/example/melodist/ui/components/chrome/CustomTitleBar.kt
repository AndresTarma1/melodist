//package com.example.melodist.ui.components.chrome
//
//import WindowMargins
//import androidx.compose.foundation.background
//import androidx.compose.foundation.combinedClickable
//import androidx.compose.foundation.hoverable
//import androidx.compose.foundation.interaction.MutableInteractionSource
//import androidx.compose.foundation.interaction.collectIsHoveredAsState
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.window.WindowDraggableArea
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material.icons.outlined.Minimize
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.awt.ComposeWindow
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.input.pointer.PointerIcon
//import androidx.compose.ui.input.pointer.pointerHoverIcon
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.window.FrameWindowScope
//import androidx.compose.ui.window.WindowPlacement
//import androidx.compose.ui.window.WindowState
//import com.sun.jna.Native
//import com.sun.jna.NativeLibrary
//import com.sun.jna.Pointer
//import com.sun.jna.platform.win32.BaseTSD.LONG_PTR
//import com.sun.jna.platform.win32.User32
//import com.sun.jna.platform.win32.WinDef.*
//import com.sun.jna.platform.win32.WinUser.*
//import com.sun.jna.win32.W32APIOptions
//import org.cef.OS.isWindows
//import org.jetbrains.jewel.window.TitleBarScope
//
//
///**
// * Custom title bar that replaces the native Windows decoration.
// *
// * Requires in your Window { } block:
// *   undecorated = true
// *   resizable   = true
// */
//@Composable
//fun CustomTitleBar(
//    scope: TitleBarScope
//) {
//    val windowHandle: HWND? = if (isWindows()) {
//        remember(window) {
//            val pointer = (window as? ComposeWindow)
//                ?.windowHandle
//                ?.let(::Pointer)
//                ?: Native.getWindowPointer(window)
//            HWND(pointer)
//        }
//    } else null
//
//
//    val surfaceColor = MaterialTheme.colorScheme.surfaceContainer
//    val onSurface    = MaterialTheme.colorScheme.onSurface
//    val primaryColor = MaterialTheme.colorScheme.primary
//
//    Surface(
//        modifier       = Modifier.fillMaxWidth().height(38.dp),
//        color          = surfaceColor,
//        tonalElevation = 1.dp
//    ) {
//        WindowDraggableArea(
//            modifier = Modifier
//                .fillMaxSize()
//                .combinedClickable(onClick = {}, onDoubleClick = {  })
//        ) {
//            Row(
//                modifier          = Modifier.fillMaxSize(),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Spacer(Modifier.width(10.dp))
//                Icon(
//                    Icons.Filled.MusicNote,
//                    contentDescription = null,
//                    tint               = primaryColor,
//                    modifier           = Modifier.size(18.dp)
//                )
//                Spacer(Modifier.width(10.dp))
//                Text(
//                    "Xd",
//                    style = MaterialTheme.typography.labelLarge.copy(
//                        fontWeight    = FontWeight.SemiBold,
//                        fontSize      = 13.sp,
//                        letterSpacing = 0.3.sp
//                    ),
//                    color    = onSurface,
//                    maxLines = 1
//                )
//                Spacer(Modifier.weight(1f))
//
//                scope.WindowBottom
//
//            }
//        }
//    }
//}
//
