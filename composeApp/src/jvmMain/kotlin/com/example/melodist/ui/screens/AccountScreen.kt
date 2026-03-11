package com.example.melodist.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.melodist.navigation.Route
import com.example.melodist.ui.components.MelodistImage
import com.example.melodist.ui.components.PlaceholderType
import com.example.melodist.utils.LocalPlayerViewModel
import com.example.melodist.viewmodels.AccountState
import com.example.melodist.viewmodels.AccountViewModel
import com.example.melodist.viewmodels.PlayerViewModel
import com.metrolist.innertube.models.PlaylistItem

// ─── Route wrapper ────────────────────────────────────────────

@Composable
fun AccountScreenRoute(
    viewModel: AccountViewModel,
    onNavigate: (Route) -> Unit,
) {

    val playerViewModel = LocalPlayerViewModel.current

    val uiState by viewModel.uiState.collectAsState()
    val cookieInput by viewModel.cookieInput.collectAsState()
    val cookieWarnings by viewModel.cookieWarnings.collectAsState()

    AccountScreen(
        uiState = uiState,
        cookieInput = cookieInput,
        cookieWarnings = cookieWarnings,
        onCookieInputChange = { viewModel.onCookieInputChange(it) },
        onLogin = { viewModel.login() },
        onLogout = { viewModel.logout() },
        onRetry = { viewModel.retry() },
        onRefreshPlaylists = { viewModel.refreshPlaylists() },
        onNavigate = onNavigate,
        playerViewModel = playerViewModel
    )
}

// ─── Main screen ──────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    uiState: AccountState,
    cookieInput: String,
    cookieWarnings: List<String> = emptyList(),
    onCookieInputChange: (String) -> Unit,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    onRetry: () -> Unit,
    onRefreshPlaylists: () -> Unit,
    onNavigate: (Route) -> Unit,
    playerViewModel: PlayerViewModel? = null
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Cuenta",
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp)
                    )
                },
                actions = {
                    if (uiState is AccountState.LoggedIn) {
                        IconButton(
                            onClick = onLogout,
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Cerrar sesión",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = uiState,
                transitionSpec = {
                    fadeIn(tween(300)) togetherWith fadeOut(tween(200))
                },
                label = "accountContent"
            ) { state ->
                when (state) {
                    is AccountState.NotLoggedIn -> LoginSection(
                        cookieInput = cookieInput,
                        cookieWarnings = cookieWarnings,
                        onCookieInputChange = onCookieInputChange,
                        onLogin = onLogin
                    )
                    is AccountState.Loading -> LoadingSection()
                    is AccountState.LoggedIn -> LoggedInSection(
                        state = state,
                        onRefresh = onRefreshPlaylists,
                        onNavigate = onNavigate,
                        playerViewModel = playerViewModel
                    )
                    is AccountState.Error -> ErrorSection(
                        message = state.message,
                        onRetry = onRetry,
                        onClear = onLogout
                    )
                    is AccountState.CookieExpired -> CookieExpiredSection(
                        cookieInput = cookieInput,
                        cookieWarnings = cookieWarnings,
                        onCookieInputChange = onCookieInputChange,
                        onRenew = onLogin,
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}

// ─── Login Section ────────────────────────────────────────────

@Composable
private fun LoginSection(
    cookieInput: String,
    cookieWarnings: List<String> = emptyList(),
    onCookieInputChange: (String) -> Unit,
    onLogin: () -> Unit
) {
    var showCookie by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // Ícono de cuenta
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(52.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Iniciar sesión",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Conecta tu cuenta de YouTube Music para ver tus playlists, historial y recomendaciones personalizadas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        // Card de instrucciones
        AnimatedVisibility(visible = showHelp) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Cómo obtener tu cookie",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    HelpStep(
                        number = "1",
                        text = "Abre music.youtube.com en tu navegador e inicia sesión"
                    )
                    HelpStep(
                        number = "2",
                        text = "Abre las herramientas de desarrollador (F12)"
                    )
                    HelpStep(
                        number = "3",
                        text = "Ve a la pestaña Application → Storage → Cookies"
                    )
                    HelpStep(
                        number = "4",
                        text = "O en la pestaña Network, busca cualquier petición a music.youtube.com y copia el valor del header 'cookie'"
                    )
                    HelpStep(
                        number = "5",
                        text = "Pega el valor completo en el campo de abajo"
                    )
                }
            }
        }

        // Botón de ayuda
        TextButton(
            onClick = { showHelp = !showHelp },
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
        ) {
            Icon(
                if (showHelp) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(if (showHelp) "Ocultar ayuda" else "¿Cómo obtengo la cookie?")
        }

        // Campo de cookie
        OutlinedTextField(
            value = cookieInput,
            onValueChange = onCookieInputChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Cookie de YouTube Music") },
            placeholder = { Text("HSID=...; SSID=...; APISID=...", style = MaterialTheme.typography.bodySmall) },
            visualTransformation = if (showCookie) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { if (cookieInput.isNotBlank()) onLogin() }),
            trailingIcon = {
                IconButton(
                    onClick = { showCookie = !showCookie },
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Icon(
                        if (showCookie) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (showCookie) "Ocultar" else "Mostrar"
                    )
                }
            },
            shape = RoundedCornerShape(14.dp),
            minLines = 2,
            maxLines = 4,
            supportingText = {
                Text(
                    "${cookieInput.length} caracteres",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )

        // Advertencias de diagnóstico de la cookie (en tiempo real)
        AnimatedVisibility(visible = cookieWarnings.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            "Advertencias sobre la cookie",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    cookieWarnings.forEach { warning ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                "•",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                warning,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Botón de inicio de sesión
        Button(
            onClick = onLogin,
            enabled = cookieInput.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .pointerHoverIcon(PointerIcon.Hand),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Iniciar sesión", fontWeight = FontWeight.SemiBold)
        }

        // Aviso de privacidad + ruta del archivo
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp).padding(top = 1.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Tu cookie se guarda localmente en este dispositivo y solo se usa para comunicarse con YouTube Music. Nunca se envía a servidores externos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Mostrar ruta del archivo de cookie
                val cookiePath = remember {
                    com.example.melodist.data.AppDirs.cookieFile.absolutePath
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        cookiePath,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun HelpStep(number: String, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                number,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

// ─── Loading Section ──────────────────────────────────────────

@Composable
private fun LoadingSection() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
            Text(
                "Verificando cuenta...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── Error Section ────────────────────────────────────────────

@Composable
private fun ErrorSection(message: String, onRetry: () -> Unit, onClear: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Error de autenticación",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onClear,
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
            ) {
                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Limpiar cookie")
            }
            Button(
                onClick = onRetry,
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Reintentar")
            }
        }
    }
}

// ─── Logged In Section ────────────────────────────────────────

@Composable
private fun LoggedInSection(
    state: AccountState.LoggedIn,
    onRefresh: () -> Unit,
    onNavigate: (Route) -> Unit,
    playerViewModel: PlayerViewModel?
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Header de perfil
        AccountProfileHeader(accountInfo = state.accountInfo)

        Spacer(Modifier.height(8.dp))

        // Sección de playlists
        PlaylistsSection(
            playlists = state.playlists,
            isLoading = state.isLoadingPlaylists,
            onRefresh = onRefresh,
            onNavigate = onNavigate
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun AccountProfileHeader(accountInfo: com.metrolist.innertube.models.AccountInfo) {
    val cookiePath = remember {
        com.example.melodist.data.AppDirs.cookieFile.absolutePath
    }
    val cookieSize = remember {
        try { com.example.melodist.data.AppDirs.cookieFile.length() } catch (_: Exception) { 0L }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                if (!accountInfo.thumbnailUrl.isNullOrBlank()) {
                    MelodistImage(
                        url = accountInfo.thumbnailUrl,
                        contentDescription = accountInfo.name,
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        placeholderType = PlaceholderType.ARTIST
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            accountInfo.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        accountInfo.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!accountInfo.email.isNullOrBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            accountInfo.email.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (!accountInfo.channelHandle.isNullOrBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            accountInfo.channelHandle.orEmpty(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                    }
                }

                // Badge de sesión activa
                AssistChip(
                    onClick = {},
                    label = { Text("Conectado", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    border = null
                )
            }

            // Barra de info: dónde está guardada la cookie
            if (cookieSize > 0L) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        "Cookie: ${cookieSize} bytes • $cookiePath",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ─── Playlists Section ────────────────────────────────────────

@Composable
private fun PlaylistsSection(
    playlists: List<PlaylistItem>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onNavigate: (Route) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header de sección
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Tus Playlists",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = onRefresh,
                enabled = !isLoading,
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refrescar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        when {
            isLoading && playlists.isEmpty() -> {
                // Skeleton loaders
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(6) { PlaylistSkeletonItem() }
                }
            }
            playlists.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            "No se encontraron playlists",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    playlists.forEach { playlist ->
                        PlaylistAccountItem(
                            playlist = playlist,
                            onClick = { onNavigate(Route.Playlist(playlist.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistAccountItem(
    playlist: PlaylistItem,
    onClick: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isHovered) MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f)
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .pointerHoverIcon(PointerIcon.Hand)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail
        MelodistImage(
            url = playlist.thumbnail,
            contentDescription = playlist.title,
            modifier = Modifier.size(52.dp),
            shape = RoundedCornerShape(10.dp),
            placeholderType = PlaceholderType.PLAYLIST
        )

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                playlist.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!playlist.author?.name.isNullOrBlank() || playlist.songCountText != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    buildString {
                        playlist.author?.name?.let { append(it) }
                        if (playlist.author?.name != null && playlist.songCountText != null) append(" • ")
                        playlist.songCountText?.let { append(it) }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

// ─── Skeleton ─────────────────────────────────────────────────

@Composable
private fun PlaylistSkeletonItem() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )
    val shimmerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha * 0.15f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(shimmerColor)
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(shimmerColor)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.35f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(shimmerColor)
            )
        }
    }
}

// ─── Cookie Expired Section ───────────────────────────────────

@Composable
private fun CookieExpiredSection(
    cookieInput: String,
    cookieWarnings: List<String>,
    onCookieInputChange: (String) -> Unit,
    onRenew: () -> Unit,
    onLogout: () -> Unit
) {
    var showCookie by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.errorContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.LockClock,
                contentDescription = null,
                modifier = Modifier.size(52.dp),
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Sesión expirada",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Tu cookie de YouTube Music ha expirado. Las cookies de sesión de Google tienen una vida útil de 2 a 4 semanas. Necesitas obtener una nueva desde tu navegador.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        "¿Por qué expiran las cookies?",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Text(
                    "Google invalida las cookies de sesión periódicamente por seguridad. No es un error de la app — es el comportamiento estándar de las cuentas de Google.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f))
                Text(
                    "Para renovarla: abre music.youtube.com en tu navegador, inicia sesión, ve a F12 → Red → cualquier petición → copia el header 'cookie' completo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        OutlinedTextField(
            value = cookieInput,
            onValueChange = onCookieInputChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nueva cookie de YouTube Music") },
            placeholder = { Text("HSID=...; SSID=...; SAPISID=...", style = MaterialTheme.typography.bodySmall) },
            visualTransformation = if (showCookie) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(
                    onClick = { showCookie = !showCookie },
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Icon(
                        if (showCookie) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null
                    )
                }
            },
            shape = RoundedCornerShape(14.dp),
            minLines = 2,
            maxLines = 4,
            supportingText = {
                Text(
                    "${cookieInput.length} caracteres",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )

        AnimatedVisibility(visible = cookieWarnings.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    cookieWarnings.forEach { warning ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("•", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer)
                            Text(warning, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Button(
            onClick = onRenew,
            enabled = cookieInput.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .pointerHoverIcon(PointerIcon.Hand),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Renovar sesión", fontWeight = FontWeight.SemiBold)
        }

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .pointerHoverIcon(PointerIcon.Hand),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Cerrar sesión")
        }

        Spacer(Modifier.height(16.dp))
    }
}
