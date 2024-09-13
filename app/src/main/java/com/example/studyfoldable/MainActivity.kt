@file:OptIn(ExperimentalWindowApi::class)

package com.example.studyfoldable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.window.area.WindowAreaCapability
import androidx.window.core.ExperimentalWindowApi
import com.example.studyfoldable.ui.theme.StudyFoldableTheme

@OptIn(ExperimentalWindowApi::class)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var foldableState by remember { mutableStateOf<FoldableState>(FoldableState.None) }
            var capabilityStatus by remember { mutableStateOf(WindowAreaCapability.Status.WINDOW_AREA_STATUS_UNSUPPORTED) }
            var displaySecondPane by remember { mutableStateOf(false) }

            GetFoldableState(this, lifecycleScope, lifecycle) { state ->
                foldableState = state
            }

            SupportFoldableLowerDisplay(
                this, lifecycleScope, lifecycle
            ) {
                displaySecondPane = it
            }


//            SupportFoldableRearDisplay(
//                activity = this,
//                lifecycleScope = lifecycleScope,
//                lifecycle = lifecycle,
//                capabilityStatusCallback = {
//                    capabilityStatus = it
//                },
//                secondScreen = { ScreenSecond() },
//            )

            StudyFoldableTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                ) { innerPadding ->
                    ScreenMain(
                        modifier = Modifier.padding(innerPadding),
                        foldableState = foldableState,
                        capabilityStatus = capabilityStatus,
                        displaySecondPane = displaySecondPane,
                    )
                }
            }
        }
    }
}