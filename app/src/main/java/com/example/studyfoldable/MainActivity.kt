@file:OptIn(ExperimentalWindowApi::class)

package com.example.studyfoldable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.window.core.ExperimentalWindowApi
import com.example.studyfoldable.ui.theme.StudyFoldableTheme

@OptIn(ExperimentalWindowApi::class)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Change this value to show the right pane WITH or WITHOUT the top bar
        val showRightPaneWithTopBar = true

        // Change this value to use or not the rear display
        val useReadDisplay = true

        setContent {
            if (useReadDisplay) {
                SupportFoldableRearDisplay(
                    activity = this,
                    lifecycleScope = lifecycleScope,
                    lifecycle = lifecycle,
                ) {
                    Page(title = "Rear", text = "Rear page content", showTopBar = true)
                }
            }

            StudyFoldableTheme {
                if (showRightPaneWithTopBar) {
                    FoldableContainer(
                        context = this,
                        lifecycleScope = lifecycleScope,
                        lifecycle = lifecycle,
                        topBar = { PageTopBar(title = "Main") },
                        contentMain = { PageContent(modifier = it, text = "Main content") },
                        contentRight = { PageContent(modifier = it, text = "Right pane") },
                        contentLower = { PageContent(modifier = it, text = "Lower pane") },
                    )
                } else {
                    FoldableContainer(
                        context = this,
                        lifecycleScope = lifecycleScope,
                        lifecycle = lifecycle,
                        contentMain = {
                            Page(
                                modifier = it,
                                title = "Main",
                                text = "Main content",
                                showTopBar = true
                            )
                        },
                        contentRight = { PageContent(modifier = it, text = "Right pane") },
                        contentLower = { PageContent(modifier = it, text = "Lower pane") },
                    )
                }
            }
        }
    }
}