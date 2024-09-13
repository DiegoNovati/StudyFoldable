package com.example.studyfoldable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.studyfoldable.ui.theme.StudyFoldableTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenSecond() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Second Screen")
                },
            )
        },
    ) { innerPadding ->
        ScreenSecondContent(
            text = "This is the second screen",
            padding = innerPadding,
        )
    }
}

@Composable
fun ScreenSecondContent(
    text: String,
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(16.dp),
) {
    Column(
        modifier = modifier
            .padding(padding)
            .padding(16.dp),
    ) {
        Text(
            text = text,
        )
    }
}

@Preview
@Composable
fun ScreenSecondPreview() {
    StudyFoldableTheme {
        ScreenSecond()
    }
}