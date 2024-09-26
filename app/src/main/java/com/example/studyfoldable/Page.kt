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

@Composable
fun Page(
    title: String,
    text: String,
    modifier: Modifier = Modifier,
    showTopBar: Boolean = true,
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        topBar = {
            if (showTopBar) {
                PageTopBar(
                    title = title,
                )
            }
        },
    ) { innerPadding ->
        PageContent(
            modifier = Modifier
                .padding(innerPadding),
            text = text,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageTopBar(
    title: String,
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text(
                text = title,
            )
        },
    )
}

@Composable
fun PageContent(
    text: String,
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues = PaddingValues(0.dp),
) {
    Column(
        modifier = modifier
            .padding(innerPadding)
            .padding(16.dp)
            .fillMaxSize(),
    ) {
        Text(
            text = text,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PageWithTopBarPreview() {
    StudyFoldableTheme {
        Page(
            title = "Page",
            text = "Page content",
            showTopBar = true,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PageWithoutTopBarPreview() {
    StudyFoldableTheme {
        Page(
            title = "Page",
            text = "Page content",
            showTopBar = false,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PageTopBarPreview() {
    StudyFoldableTheme {
        PageTopBar(
            title = "Title",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PageContentPreview() {
    StudyFoldableTheme {
        PageContent(
            text = "This is the content of the page",
        )
    }
}