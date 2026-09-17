package com.maxim.cobaltyt

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var store: LocalStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = LocalStore(this)
        setContent { CobaltYTApp(store) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CobaltYTApp(store: LocalStore) {
    var tab by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<HistoryItem?>(null) }
    var searchText by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<VideoItem>>(emptyList()) }
    var history by remember { mutableStateOf(store.history()) }
    var cobaltUrl by remember { mutableStateOf(store.cobaltUrl()) }
    var youtubeKey by remember { mutableStateOf(store.youtubeKey()) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val cobalt = remember(store) { CobaltApi(store) }
    val yt = remember(store) { YouTubeSearch(store) }
    val downloader = remember(store) { DownloadHelper((androidx.compose.ui.platform.LocalContext.current)) }

    MaterialTheme(colorScheme = lightColorScheme()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Cobalt YT") },
                    actions = {
                        IconButton(onClick = { tab = 3 }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(tab == 0, { tab = 0 }, icon = { Icon(Icons.Default.Search, null) }, label = { Text("Search") })
                    NavigationBarItem(tab == 1, { tab = 1 }, icon = { Icon(Icons.Default.History, null) }, label = { Text("History") })
                    NavigationBarItem(tab == 2, { tab = 2 }, icon = { Icon(Icons.Default.Download, null) }, label = { Text("Offline") })
                    NavigationBarItem(tab == 3, { tab = 3 }, icon = { Icon(Icons.Default.Settings, null) }, label = { Text("Settings") })
                }
            }
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                when (tab) {
                    0 -> SearchScreen(
                        searchText, { searchText = it },
                        results,
                        onSearch = {
                            scope.launch {
                                yt.search(searchText).onSuccess { results = it }
                                    .onFailure { message = it.message }
                            }
                        },
                        onOpen = { video ->
                            scope.launch {
                                cobalt.resolve(video.youtubeUrl).onSuccess { media ->
                                    val item = HistoryItem(video.id, video.title, video.channel, video.youtubeUrl, media, System.currentTimeMillis())
                                    store.addHistory(item)
                                    history = store.history()
                                    selected = item
                                }.onFailure { message = it.message }
                            }
                        },
                        onDownload = { video ->
                            scope.launch {
                                cobalt.resolve(video.youtubeUrl).onSuccess { media ->
                                    downloader.enqueue(media, video.title)
                                    message = "Download started"
                                }.onFailure { message = it.message }
                            }
                        },
                        message = message
                    )
                    1 -> HistoryScreen(history, onOpen = { selected = it }, onClear = {
                        store.clearHistory(); history = emptyList()
                    })
                    2 -> OfflineScreen()
                    3 -> SettingsScreen(
                        cobaltUrl, { cobaltUrl = it },
                        youtubeKey, { youtubeKey = it },
                        onSave = {
                            store.setCobaltUrl(cobaltUrl)
                            store.setYoutubeKey(youtubeKey)
                            message = "Settings saved"
                        },
                        message = message
                    )
                }
                selected?.let { item ->
                    PlayerDialog(item, onDismiss = { selected = null })
                }
            }
        }
    }
}

@Composable
fun SearchScreen(
    query: String,
    onQuery: (String) -> Unit,
    results: List<VideoItem>,
    onSearch: () -> Unit,
    onOpen: (VideoItem) -> Unit,
    onDownload: (VideoItem) -> Unit,
    message: String?
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(query, onQuery, Modifier.weight(1f), singleLine = true, label = { Text("Search YouTube") })
            Spacer(Modifier.width(8.dp))
            Button(onClick = onSearch) { Text("Search") }
        }
        Spacer(Modifier.height(12.dp))
        Text("No Shorts feed — choose a video and open it.", style = MaterialTheme.typography.bodyMedium)
        message?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp)) }
        LazyColumn {
            items(results) { video ->
                Card(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                    Row(
                        Modifier.fillMaxWidth().clickable { onOpen(video) }.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(video.title, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(video.channel, style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { onDownload(video) }) {
                            Icon(Icons.Default.Download, "Download")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryScreen(history: List<HistoryItem>, onOpen: (HistoryItem) -> Unit, onClear: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("History", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onClear) { Text("Clear") }
        }
        if (history.isEmpty()) Text("Nothing watched yet.")
        LazyColumn {
            items(history) { item ->
                ListItem(
                    headlineContent = { Text(item.title, maxLines = 2, overflow = TextOverflow.Ellipsis) },
                    supportingContent = { Text(item.channel) },
                    modifier = Modifier.clickable { onOpen(item) }
                )
            }
        }
    }
}

@Composable
fun OfflineScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Offline", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("Downloads are saved to Downloads/Cobalt YT using Android Download Manager.")
        Spacer(Modifier.height(12.dp))
        Text("Open your Android Downloads app to see completed files.")
    }
}

@Composable
fun SettingsScreen(
    cobaltUrl: String, onCobaltUrl: (String) -> Unit,
    youtubeKey: String, onYoutubeKey: (String) -> Unit,
    onSave: () -> Unit, message: String?
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = cobaltUrl, onValueChange = onCobaltUrl,
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            label = { Text("Cobalt API URL") },
            supportingText = { Text("Example: http://10.0.2.2:9000/ for Android Emulator") }
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = youtubeKey, onValueChange = onYoutubeKey,
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            label = { Text("YouTube Data API v3 key") }
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = onSave) { Text("Save") }
        message?.let { Text(it, modifier = Modifier.padding(top = 12.dp)) }
        Spacer(Modifier.height(20.dp))
        Text("Cobalt YT intentionally has no endless Shorts feed.")
    }
}

@Composable
fun PlayerDialog(item: HistoryItem, onDismiss: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val player = remember(item.mediaUrl) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(item.mediaUrl))
            prepare()
            playWhenReady = true
        }
    }
    DisposableEffect(player) { onDispose { player.release() } }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text(item.title, maxLines = 2, overflow = TextOverflow.Ellipsis) },
        text = {
            AndroidView(
                factory = { PlayerView(it).apply { this.player = player } },
                modifier = Modifier.fillMaxWidth().height(220.dp)
            )
        }
    )
}
