package com.example.studyfoldable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.window.area.WindowAreaCapability
import androidx.window.area.WindowAreaController
import androidx.window.area.WindowAreaInfo
import androidx.window.area.WindowAreaSession
import com.example.studyfoldable.ui.theme.StudyFoldableTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

class MainActivity : ComponentActivity() {

    private lateinit var windowAreaController: WindowAreaController
    private lateinit var displayExecutor: Executor
    private var windowAreaSession: WindowAreaSession? = null
    private var windowAreaInfo: WindowAreaInfo? = null

    private val dualScreenOperation = WindowAreaCapability.Operation.OPERATION_PRESENT_ON_AREA
    private val rearDisplayOperation =
        WindowAreaCapability.Operation.OPERATION_TRANSFER_ACTIVITY_TO_AREA

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        getFoldableState(this, lifecycleScope, lifecycle) { foldableState ->
            println("---------- ----------")
            println("foldableState: $foldableState")
        }

        // ********** ********** ********** ********** **********

        displayExecutor = ContextCompat.getMainExecutor(this)
        windowAreaController = WindowAreaController.getOrCreate()

//        val operation = WindowAreaCapability.Operation.OPERATION_TRANSFER_ACTIVITY_TO_AREA
        val operation = WindowAreaCapability.Operation.OPERATION_PRESENT_ON_AREA

        // ********** ********** ********** ********** **********

        setContent {
            var capabilityStatus by remember { mutableStateOf(WindowAreaCapability.Status.WINDOW_AREA_STATUS_UNSUPPORTED) }

            LaunchedEffect(true) {
                lifecycleScope.launch(Dispatchers.Main) {
                    lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        windowAreaController.windowAreaInfos
                            .map { info -> info.firstOrNull { it.type == WindowAreaInfo.Type.TYPE_REAR_FACING } }
                            .onEach { info -> windowAreaInfo = info }
                            .map {
                                it?.getCapability(operation)?.status
                                    ?: WindowAreaCapability.Status.WINDOW_AREA_STATUS_UNSUPPORTED
                            }
                            .distinctUntilChanged()
                            .collect {
                                capabilityStatus = it
                                println("---------- capabilityStatus = $capabilityStatus")
                            }
                    }
                }
            }

            StudyFoldableTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "capabilityStatus = $capabilityStatus",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "$name!",
        modifier = modifier
            .padding(16.dp),
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    StudyFoldableTheme {
        Greeting("Android")
    }
}