package com.worldvisionsoft.knowledgehub.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun ImageDetailScreen(imageUrl: String, tags: String, user: String) {
    Scaffold { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                contentScale = ContentScale.Fit
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Uploaded by: $user", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Tags: $tags", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
