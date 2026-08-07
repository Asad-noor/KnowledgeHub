package com.worldvisionsoft.knowledgehub.view.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.worldvisionsoft.knowledgehub.view.ImageSearchScreen
import com.worldvisionsoft.knowledgehub.view.ImageDetailScreen
import com.worldvisionsoft.knowledgehub.viewmodel.ImageViewModel

@Composable
fun AppNavigation(viewModel: ImageViewModel) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = "search"
    ) {
        composable("search") {
            ImageSearchScreen(
                viewModel = viewModel,
                onImageClick = { hit ->
                    viewModel.selectHit(hit)
                    navController.navigate("detail")
                }
            )
        }

        composable("detail") {
            uiState.selectedHit?.let { hit ->
                ImageDetailScreen(
                    imageUrl = hit.largeImageURL,
                    tags = hit.tags,
                    user = hit.user
                )
            }
        }
    }
}
