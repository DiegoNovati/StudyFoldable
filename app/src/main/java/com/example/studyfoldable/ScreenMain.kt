package com.example.studyfoldable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.area.WindowAreaCapability
import androidx.window.core.ExperimentalWindowApi
import com.example.studyfoldable.ui.theme.StudyFoldableTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalWindowApi::class)
@Composable
fun ScreenMain(
    foldableState: FoldableState,
    capabilityStatus: WindowAreaCapability.Status,
    displaySecondPane: Boolean,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Main Screen")
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1F)
                    .padding(16.dp),
            ) {
                StateText(
                    label = "Capability Status",
                    value = capabilityStatus.toString(),
                )
                StateText(
                    label = "Foldable State",
                    value = foldableState.toString(),
                )
            }
            if (displaySecondPane) {
                Column(
                    modifier = Modifier.weight(1F),
                ) {
                    ScreenSecond()
                }
            }
        }
    }
}

@Composable
private fun StateText(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
        )
        Text(
            text = value,
            textAlign = TextAlign.End,
        )
    }
}

@OptIn(ExperimentalWindowApi::class)
@Preview
@Composable
fun MainScreenWithoutSecondPanePreview() {
    StudyFoldableTheme {
        ScreenMain(
            foldableState = FoldableState.None,
            capabilityStatus = WindowAreaCapability.Status.WINDOW_AREA_STATUS_UNSUPPORTED,
            displaySecondPane = false,
        )
    }
}

@OptIn(ExperimentalWindowApi::class)
@Preview
@Composable
fun MainScreenWithSecondPanePreview() {
    StudyFoldableTheme {
        ScreenMain(
            foldableState = FoldableState.None,
            capabilityStatus = WindowAreaCapability.Status.WINDOW_AREA_STATUS_UNSUPPORTED,
            displaySecondPane = true,
        )
    }
}