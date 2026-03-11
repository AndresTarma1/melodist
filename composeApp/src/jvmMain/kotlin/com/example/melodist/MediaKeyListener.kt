package com.example.melodist

import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.NativeHookException
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Captura teclas multimedia globales del teclado (Play/Pause, Next, Previous, Stop)
 * usando JNativeHook para que funcionen incluso cuando la app no tiene foco.
 */
class MediaKeyListener(
    private val onPlayPause: () -> Unit,
    private val onNext: () -> Unit,
    private val onPrevious: () -> Unit,
    private val onStop: () -> Unit,
) : NativeKeyListener {

    private val log = Logger.getLogger("MediaKeyListener")

    fun register() {
        try {
            // Silenciar los logs internos de JNativeHook (muy verbose)
            Logger.getLogger(GlobalScreen::class.java.`package`.name).apply {
                level = Level.OFF
                useParentHandlers = false
            }

            GlobalScreen.registerNativeHook()
            GlobalScreen.addNativeKeyListener(this)
            log.info("Media keys registradas correctamente")
        } catch (e: NativeHookException) {
            log.log(Level.WARNING, "No se pudieron registrar media keys globales: ${e.message}")
        } catch (e: UnsatisfiedLinkError) {
            log.log(Level.WARNING, "JNativeHook no disponible en esta plataforma: ${e.message}")
        }
    }

    fun unregister() {
        try {
            GlobalScreen.removeNativeKeyListener(this)
            GlobalScreen.unregisterNativeHook()
            log.info("Media keys des-registradas")
        } catch (_: Exception) { }
    }

    override fun nativeKeyPressed(event: NativeKeyEvent) {
        when (event.keyCode) {
            NativeKeyEvent.VC_MEDIA_PLAY  -> onPlayPause()
            NativeKeyEvent.VC_MEDIA_STOP  -> onStop()
            NativeKeyEvent.VC_MEDIA_NEXT  -> onNext()
            NativeKeyEvent.VC_MEDIA_PREVIOUS -> onPrevious()
        }
    }

    override fun nativeKeyReleased(event: NativeKeyEvent) { }
    override fun nativeKeyTyped(event: NativeKeyEvent) { }
}

