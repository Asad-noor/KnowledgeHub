package com.worldvisionsoft.knowledgehub.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.worldvisionsoft.knowledgehub.model.remote.dtos.Hit
import com.worldvisionsoft.knowledgehub.viewmodel.ImageViewModel

@Composable
fun ImageSearchScreen(
    viewModel: ImageViewModel,
    onImageClick: (Hit) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TextField(
                value = query,
                onValueChange = {
                    query = it
                    viewModel.updateQuery(it)
                },
                placeholder = { Text("Search images...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp)
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.error.isNotEmpty() -> {
                Box(Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(uiState.error, color = MaterialTheme.colorScheme.error)
                }
            }
            uiState.data.isNullOrEmpty() -> {
                Box(Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Type something to search!")
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.padding(innerPadding).fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.data!!) { image ->
                        ImageCard(image = image, onClick = { onImageClick(image) })
                    }
                }
            }
        }
    }
}

@Composable
fun ImageCard(image: Hit, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            AsyncImage(
                model = image.largeImageURL,
                contentDescription = image.tags,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
            Text(
                text = "Tags: ${image.tags}",
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
