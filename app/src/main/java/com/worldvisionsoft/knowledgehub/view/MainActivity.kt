package com.worldvisionsoft.knowledgehub.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.worldvisionsoft.knowledgehub.view.navigation.AppNavigation
import com.worldvisionsoft.knowledgehub.view.theme.KnowledgeHubTheme
import com.worldvisionsoft.knowledgehub.viewmodel.ImageViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: ImageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KnowledgeHubTheme {
                AppNavigation(viewModel)
            }
        }
    }
}
