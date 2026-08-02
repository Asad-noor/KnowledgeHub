package com.worldvisionsoft.knowledgehub.view

import android.app.appsearch.SearchResult
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.State
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.Image
import coil3.compose.AsyncImage
import com.worldvisionsoft.knowledgehub.model.remote.ImageRepository
import com.worldvisionsoft.knowledgehub.view.navigation.NavigationArguments
import com.worldvisionsoft.knowledgehub.view.theme.KnowledgeHubTheme
import com.worldvisionsoft.knowledgehub.viewmodel.ImageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlin.getValue

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    val viewModel: ImageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KnowledgeHubTheme {
                NavigationArguments()
            }
        }
    }
}

@Composable
fun KnowledgeHubTheme(viewModel: ImageViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }

    Scaffold(modifier = Modifier.fillMaxSize(),
        topBar = {
            TextField(value = query, onValueChange = {
                query = it
                viewModel.updateQuery(it)
            }, modifier = Modifier.fillMaxWidth().statusBarsPadding(),
                colors = TextFieldDefaults.colors(
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                )
            )
        }) { innerPadding ->
        if (uiState.isLoading) {
            Box(Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error.isNotEmpty()) {
            Box(Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center) {
                Text(uiState.error)
            }
        } else uiState.data?.let { data ->
            LazyColumn(modifier = Modifier.padding(innerPadding).padding(horizontal = 16.dp).fillMaxSize()) {
                items(data) { image ->
                    AsyncImage(
                        model = image.largeImageURL,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
fun SearchScreen(viewModel: ImageViewModel) {

    var query by rememberSaveable {
        mutableStateOf("")
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(query) {
        delay(500)
        viewModel.updateQuery(query)
    }
}

//@Composable
//fun loadNetworkImage(
//    url: String,
//    imageRepository: ImageRepository
//): State<Result<Image>> {
//    // Creates a State<T> with Result.Loading as initial value
//    // If either `url` or `imageRepository` changes, the running producer
//    // will cancel and will be re-launched with the new inputs.
//    return produceState<Result<Image>>(initialValue = Result.Loading, url, imageRepository) {
//        // In a coroutine, can make suspend calls
//        val image = imageRepository.load(url)
//
//        // Update State with either an Error or Success result.
//        // This will trigger a recomposition where this State is read
//        value = if (image == null) {
//            Result.Error
//        } else {
//            Result.Success(image)
//        }
//    }
//}
