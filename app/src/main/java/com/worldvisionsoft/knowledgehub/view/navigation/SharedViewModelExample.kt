package com.worldvisionsoft.knowledgehub.view.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController

@Composable
fun SharedViewModelNav() {

    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "onboarding" //like a onboarding feature
    ) {
        navigation(
            startDestination = "screen1",
            route = "onboarding"
        ) {

        }
    }
}
