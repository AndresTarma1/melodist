package com.example.melodist.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import com.example.melodist.data.account.AccountManager
import com.example.melodist.data.repository.MusicRepository
import com.example.melodist.data.repository.SearchRepository
import com.example.melodist.viewmodels.*
import kotlinx.serialization.serializer

class RootComponent(
    componentContext: ComponentContext,
    private val musicRepository: MusicRepository? = null,
    private val searchRepository: SearchRepository? = null
) : ComponentContext by componentContext {

    private val navigation = StackNavigation<ScreenConfig>()

    val childStack: Value<ChildStack<ScreenConfig, Child>> =
        childStack(
            source = navigation,
            serializer = serializer<ScreenConfig>(),
            initialConfiguration = ScreenConfig.Home,
            handleBackButton = true,
            childFactory = ::createChild
        )

    private fun createChild(config: ScreenConfig, componentContext: ComponentContext): Child {
        return when (config) {
            is ScreenConfig.Home -> Child.Home(HomeComponent(componentContext))
            is ScreenConfig.Search -> Child.Search(SearchComponent(componentContext, searchRepository))
            is ScreenConfig.Library -> Child.Library(LibraryComponent(componentContext, musicRepository))
            is ScreenConfig.Account -> Child.Account(AccountComponent(componentContext))
            is ScreenConfig.Settings -> Child.Settings
            is ScreenConfig.Album -> Child.Album(AlbumComponent(componentContext, config.browseId, musicRepository))
            is ScreenConfig.Playlist -> Child.Playlist(PlaylistComponent(componentContext, config.playlistId, musicRepository))
            is ScreenConfig.Artist -> Child.Artist(ArtistComponent(componentContext, config.artistId, musicRepository))
        }
    }

    fun navigateTo(config: ScreenConfig) {
        navigation.navigate { stack ->
            // Remove any existing instance of this config, then push it on top
            stack.filterNot { it == config } + config
        }
    }

    fun onBack() {
        navigation.pop()
    }

    fun switchTab(config: ScreenConfig) {
        navigation.replaceAll(config)
    }

    // ─── Child sealed class ───

    sealed class Child {
        data class Home(val component: HomeComponent) : Child()
        data class Search(val component: SearchComponent) : Child()
        data class Library(val component: LibraryComponent) : Child()
        data class Account(val component: AccountComponent) : Child()
        data object Settings : Child()
        data class Album(val component: AlbumComponent) : Child()
        data class Playlist(val component: PlaylistComponent) : Child()
        data class Artist(val component: ArtistComponent) : Child()
    }
}

// ─── Screen Components ─────────────────────────────────
// Each component owns its ViewModel so it survives back-stack

class HomeComponent(componentContext: ComponentContext) : ComponentContext by componentContext {
    val viewModel = HomeViewModel(loginState = AccountManager.loginState)
}

class SearchComponent(
    componentContext: ComponentContext,
    searchRepository: SearchRepository?
) : ComponentContext by componentContext {
    val viewModel = SearchViewModel(searchRepository!!)
}

class LibraryComponent(
    componentContext: ComponentContext,
    repository: MusicRepository?
) : ComponentContext by componentContext {
    val viewModel = LibraryViewModel(repository!!, loginState = AccountManager.loginState)
}

class AlbumComponent(
    componentContext: ComponentContext,
    val browseId: String,
    repository: MusicRepository?
) : ComponentContext by componentContext {
    val viewModel = AlbumViewModel(repository!!).also { it.loadAlbum(browseId) }
}

class PlaylistComponent(
    componentContext: ComponentContext,
    val playlistId: String,
    repository: MusicRepository?
) : ComponentContext by componentContext {
    val viewModel = PlaylistViewModel(repository!!).also { it.loadPlaylist(playlistId) }
}

class ArtistComponent(
    componentContext: ComponentContext,
    val artistId: String,
    repository: MusicRepository?
) : ComponentContext by componentContext {
    val viewModel = ArtistViewModel(repository!!).also { it.loadArtist(artistId) }
}

class AccountComponent(componentContext: ComponentContext) : ComponentContext by componentContext {
    val viewModel = AccountViewModel()
}

