package app.ninetunes.android.ui.screens.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import app.ninetunes.android.Database
import app.ninetunes.android.LocalPlayerAwareWindowInsets
import app.ninetunes.android.LocalPlayerServiceBinder
import app.ninetunes.android.R
import app.ninetunes.android.models.Song
import app.ninetunes.android.ui.components.LocalMenuState
import app.ninetunes.android.ui.components.themed.FloatingActionsContainerWithScrollToTop
import app.ninetunes.android.ui.components.themed.Header
import app.ninetunes.android.ui.components.themed.InHistoryMediaItemMenu
import app.ninetunes.android.ui.components.themed.SecondaryTextButton
import app.ninetunes.android.ui.items.SongItem
import app.ninetunes.android.utils.align
import app.ninetunes.android.utils.asMediaItem
import app.ninetunes.android.utils.forcePlay
import app.ninetunes.android.utils.medium
import app.ninetunes.compose.persist.persistList
import app.ninetunes.core.ui.Dimensions
import app.ninetunes.core.ui.LocalAppearance
import app.ninetunes.providers.innertube.models.NavigationEndpoint
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LocalSongSearch(
    textFieldValue: TextFieldValue,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    decorationBox: @Composable (@Composable () -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val (colorPalette, typography) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current

    var items by persistList<Song>("search/local/songs")

    LaunchedEffect(textFieldValue.text) {
        if (textFieldValue.text.length > 1)
            Database
                .search("%${textFieldValue.text}%")
                .collect { items = it.toImmutableList() }
    }

    val lazyListState = rememberLazyListState()

    Box(modifier = modifier) {
        LazyColumn(
            state = lazyListState,
            contentPadding = LocalPlayerAwareWindowInsets.current
                .only(WindowInsetsSides.Vertical + WindowInsetsSides.End).asPaddingValues(),
            modifier = Modifier.fillMaxSize()
        ) {
            item(
                key = "header",
                contentType = 0
            ) {
                Header(
                    titleContent = {
                        BasicTextField(
                            value = textFieldValue,
                            onValueChange = onTextFieldValueChange,
                            textStyle = typography.xxl.medium.align(TextAlign.End),
                            singleLine = true,
                            maxLines = 1,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            cursorBrush = SolidColor(colorPalette.text),
                            decorationBox = decorationBox
                        )
                    },
                    actionsContent = {
                        if (textFieldValue.text.isNotEmpty()) SecondaryTextButton(
                            text = stringResource(R.string.clear),
                            onClick = { onTextFieldValueChange(TextFieldValue()) }
                        )
                    }
                )
            }

            items(
                items = items,
                key = Song::id
            ) { song ->
                SongItem(
                    modifier = Modifier
                        .combinedClickable(
                            onLongClick = {
                                menuState.display {
                                    InHistoryMediaItemMenu(
                                        song = song,
                                        onDismiss = menuState::hide
                                    )
                                }
                            },
                            onClick = {
                                val mediaItem = song.asMediaItem
                                binder?.stopRadio()
                                binder?.player?.forcePlay(mediaItem)
                                binder?.setupRadio(
                                    NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId)
                                )
                            }
                        )
                        .animateItem(),
                    song = song,
                    thumbnailSize = Dimensions.thumbnails.song
                )
            }
        }

        FloatingActionsContainerWithScrollToTop(lazyListState = lazyListState)
    }
}
