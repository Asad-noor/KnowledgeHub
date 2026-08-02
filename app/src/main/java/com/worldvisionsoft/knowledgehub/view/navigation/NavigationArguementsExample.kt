package com.worldvisionsoft.knowledgehub.view.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.worldvisionsoft.knowledgehub.view.Screen1
import com.worldvisionsoft.knowledgehub.view.Screen2

@Composable
fun NavigationArguments() {

    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "screen1"
    ) {
        composable("screen1") {
            Screen1(onNavigateToScreen2 = {
                navController.navigate("screen2/$it")
            })
        }

        composable(
            "screen2/{my_param}",
            arguments = listOf(
                navArgument("my_param") {
                    type = NavType.StringType
                }
            ))
        {
            val param = it.arguments?.getString("my_param") ?: ""
            Screen2(param)
        }
    }
}