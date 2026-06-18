package com.taskalon.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.taskalon.app.ui.theme.TaskalonTheme

/**
 * Placeholder app shell. This compiles and launches a simple landing screen so CI is green;
 * the real Library / task screens land when the design handoff is implemented.
 */
@Composable
fun TaskalonApp() {
    TaskalonTheme {
        Scaffold { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Taskalon",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = if (BuildConfig.IS_QA) "QA build" else "Local-first tasks for Android",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}
