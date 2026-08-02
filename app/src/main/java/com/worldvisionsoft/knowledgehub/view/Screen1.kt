package com.worldvisionsoft.knowledgehub.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Screen1(onNavigateToScreen2: (String) -> Unit) {
    Box(modifier = Modifier.padding(30.dp)) {
        Button(onClick = {
            onNavigateToScreen2("This is the arg String")
        }) {
            Text("Screen 1 Button")
        }
    }
}