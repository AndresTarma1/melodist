package com.example.melodist.data

import java.io.File
import java.util.logging.Logger

/**
 * Centraliza la creación de todas las carpetas que la app necesita antes de
 * que arranque cualquier módulo (BD, preferencias, caché de imágenes, descargas).
 *
 * Llama a [ensureDirectories] como PRIMERA instrucción en main() para que la
 * app instalada pueda crear sus rutas de datos aunque nunca se haya ejecutado antes.
 */
object AppDirs {

    private val log = Logger.getLogger("AppDirs")

    // ── Raíces ────────────────────────────────────────────────────────────────

    /**
     * Directorio raíz de datos persistentes: ~/.melodist
     * (en Windows: C:\Users\<user>\.melodist)
     */
    val dataRoot: File by lazy {
        File(System.getProperty("user.home"), ".melodist")
    }

    /**
     * Directorio de configuración del sistema operativo:
     *  - Windows: %APPDATA%\melodist
     *  - Linux/macOS: ~/.config/melodist
     */
    val configRoot: File by lazy {
        val base = System.getenv("APPDATA")
            ?: (System.getProperty("user.home") + File.separator + ".config")
        File(base, "melodist")
    }

    // ── Subdirectorios ────────────────────────────────────────────────────────

    /** Base de datos SQLite */
    val databaseDir: File get() = dataRoot

    /** Caché de imágenes de Coil */
    val imageCacheDir: File get() = File(dataRoot, "image_cache")

    /** Canciones descargadas */
    val songsDir: File get() = File(dataRoot, "cache/songs")

    /** Directorio temporal propio (para el driver SQLite nativo y otros) */
    val tmpDir: File get() = File(dataRoot, "tmp")

    /** Preferencias (settings.properties) */
    val preferencesFile: File get() = File(dataRoot, "settings.properties")

    /** Archivo de cookie de YouTube */
    val cookieFile: File get() = File(configRoot, "yt_cookie.txt")

    // ── Inicialización ────────────────────────────────────────────────────────

    /**
     * Crea **todas** las carpetas necesarias.
     * Es seguro llamarlo múltiples veces (idempotente).
     * Se capturan errores para no crashear si hay algún problema de permisos.
     */
    fun ensureDirectories() {
        listOf(dataRoot, configRoot, imageCacheDir, songsDir, tmpDir).forEach { dir ->
            try {
                if (!dir.exists()) {
                    val created = dir.mkdirs()
                    log.info("Creada carpeta: ${dir.absolutePath} (ok=$created)")
                }
            } catch (e: Exception) {
                log.severe("Error creando ${dir.absolutePath}: ${e.message}")
            }
        }
        log.info("Directorios verificados: dataRoot=${dataRoot.exists()}, configRoot=${configRoot.exists()}, songs=${songsDir.exists()}")
    }
}




