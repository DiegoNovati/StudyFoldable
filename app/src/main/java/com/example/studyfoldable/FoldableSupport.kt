@file:OptIn(ExperimentalWindowApi::class)

package com.example.studyfoldable

import android.content.Context
import android.graphics.Rect
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.window.area.WindowAreaCapability
import androidx.window.area.WindowAreaController
import androidx.window.area.WindowAreaInfo
import androidx.window.area.WindowAreaPresentationSessionCallback
import androidx.window.area.WindowAreaSession
import androidx.window.area.WindowAreaSessionPresenter
import androidx.window.core.ExperimentalWindowApi
import androidx.window.layout.FoldingFeature
import androidx.window.layout.FoldingFeature.OcclusionType
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import com.example.studyfoldable.ui.theme.StudyFoldableTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

// Ref: https://developer.android.com/guide/topics/large-screens/make-apps-fold-aware

// Note: this container displays the right content WITHOUT the top bar
@Composable
fun FoldableContainer(
    context: Context,
    lifecycleScope: LifecycleCoroutineScope,
    lifecycle: Lifecycle,
    contentMain: @Composable (modifier: Modifier) -> Unit,
    contentLower: (@Composable (modifier: Modifier) -> Unit)? = null,
    contentRight: (@Composable (modifier: Modifier) -> Unit)? = null,
) {
    var foldablePanes by remember { mutableStateOf(FoldablePanes.None) }

    SupportFoldableDisplay(
        context = context,
        lifecycleScope = lifecycleScope,
        lifecycle = lifecycle,
    ) {
        foldablePanes = it
    }

    FoldableContainer(
        foldablePanes = foldablePanes,
        contentMain = contentMain,
        contentLower = contentLower,
        contentRight = contentRight,
    )
}

// Note: this container displays the right content WITH the top bar
@Composable
fun FoldableContainer(
    context: Context,
    lifecycleScope: LifecycleCoroutineScope,
    lifecycle: Lifecycle,
    topBar: @Composable () -> Unit,
    contentMain: @Composable (modifier: Modifier) -> Unit,
    contentLower: (@Composable (modifier: Modifier) -> Unit)? = null,
    contentRight: (@Composable (modifier: Modifier) -> Unit)? = null,
) {
    var foldablePanes by remember { mutableStateOf(FoldablePanes.None) }

    SupportFoldableDisplay(
        context = context,
        lifecycleScope = lifecycleScope,
        lifecycle = lifecycle,
    ) {
        foldablePanes = it
    }

    FoldableContainer(
        foldablePanes = foldablePanes,
        topBar = topBar,
        contentMain = contentMain,
        contentLower = contentLower,
        contentRight = contentRight,
    )
}

enum class FoldableStateType {
    Flat, HalfOpened,
}

data class FoldableStateInfo(
    val type: FoldableStateType,
    val orientation: FoldingFeature.Orientation,
    val bounds: Rect,
    val isSeparating: Boolean,
    val occlusionType: OcclusionType,
)

sealed interface FoldableState {
    data object None : FoldableState // Not a foldable device or foldable folded
    data class Foldable(val foldedStateList: List<FoldableStateInfo>) : FoldableState
}

@Composable
fun GetFoldableState(
    context: Context,
    lifecycleScope: LifecycleCoroutineScope,
    lifecycle: Lifecycle,
    callback: (FoldableState) -> Unit,
) {
    LaunchedEffect(true) {
        lifecycleScope.launch(Dispatchers.Main) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                WindowInfoTracker.getOrCreate(context)
                    .windowLayoutInfo(context)
                    .collect { newLayoutInfo ->
                        callback(newLayoutInfo.toFoldableState())
                    }
            }
        }
    }
}

private enum class FoldablePanes {
    None, Lower, Right,
}

@Composable
private fun FoldableContainer(
    foldablePanes: FoldablePanes,
    contentMain: @Composable (modifier: Modifier) -> Unit,
    contentLower: (@Composable (modifier: Modifier) -> Unit)? = null,
    contentRight: (@Composable (modifier: Modifier) -> Unit)? = null,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Row(
            modifier = Modifier
                .weight(1F),
        ) {
            contentMain(
                Modifier
                    .weight(1f)
                    .fillMaxSize(),
            )
            if (foldablePanes == FoldablePanes.Right) {
                contentRight?.invoke(Modifier.weight(1f))
            }
        }
        if (foldablePanes == FoldablePanes.Lower) {
            contentLower?.invoke(Modifier.weight(1f))
        }
    }
}

@Composable
private fun FoldableContainer(
    foldablePanes: FoldablePanes,
    topBar: @Composable () -> Unit,
    contentMain: @Composable (modifier: Modifier) -> Unit,
    contentLower: (@Composable (modifier: Modifier) -> Unit)? = null,
    contentRight: (@Composable (modifier: Modifier) -> Unit)? = null,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Scaffold(
            modifier = Modifier.weight(1f),
            topBar = topBar,
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .padding(innerPadding)
                    .weight(1f),
            ) {
                contentMain(Modifier.weight(1f))
                if (foldablePanes == FoldablePanes.Right) {
                    contentRight?.invoke(Modifier.weight(1f))
                }
            }
        }
        if (foldablePanes == FoldablePanes.Lower) {
            contentLower?.invoke(Modifier.weight(1f))
        }
    }
}

@Composable
private fun SupportFoldableDisplay(
    context: Context,
    lifecycleScope: LifecycleCoroutineScope,
    lifecycle: Lifecycle,
    showFoldablePanesCallback: (FoldablePanes) -> Unit,
) {
    GetFoldableState(
        context = context,
        lifecycleScope = lifecycleScope,
        lifecycle = lifecycle,
    ) { foldableState ->
        if (foldableState is FoldableState.Foldable) {
            val foldableStateInfo = foldableState.foldedStateList.lastOrNull()
            if (
                foldableStateInfo?.type == FoldableStateType.HalfOpened &&
                foldableStateInfo.orientation == FoldingFeature.Orientation.HORIZONTAL
            ) {
                showFoldablePanesCallback(FoldablePanes.Lower)
            } else if (foldableStateInfo?.type == FoldableStateType.Flat &&
                foldableStateInfo.orientation == FoldingFeature.Orientation.VERTICAL
            ) {
                showFoldablePanesCallback(FoldablePanes.Right)
            } else {
                showFoldablePanesCallback(FoldablePanes.None)
            }
        }
    }
}

@Composable
fun SupportFoldableRearDisplay(
    activity: ComponentActivity,
    lifecycleScope: LifecycleCoroutineScope,
    lifecycle: Lifecycle,
    capabilityStatusCallback: ((WindowAreaCapability.Status) -> Unit)? = null,
    rearScreen: @Composable () -> Unit,
) {
    readDisplayDisplayExecutor = ContextCompat.getMainExecutor(activity)
    readDisplayWindowAreaController = WindowAreaController.getOrCreate()

    LaunchedEffect(true) {
        lifecycleScope.launch(Dispatchers.Main) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                readDisplayWindowAreaController.windowAreaInfos
                    .map { info -> info.firstOrNull { it.type == WindowAreaInfo.Type.TYPE_REAR_FACING } }
                    .onEach { info -> readDisplayWindowAreaInfo = info }
                    .map {
                        it?.getCapability(readDisplayOperation)?.status
                            ?: WindowAreaCapability.Status.WINDOW_AREA_STATUS_UNSUPPORTED
                    }
                    .distinctUntilChanged()
                    .collect { capabilityStatus ->
                        capabilityStatusCallback?.invoke(capabilityStatus)
                        toggleDualScreenMode(activity, rearScreen)
                    }
            }
        }
    }
}

private lateinit var readDisplayWindowAreaController: WindowAreaController
private lateinit var readDisplayDisplayExecutor: Executor
private var readDisplayWindowAreaSession: WindowAreaSession? = null
private var readDisplayWindowAreaInfo: WindowAreaInfo? = null
private val readDisplayOperation = WindowAreaCapability.Operation.OPERATION_PRESENT_ON_AREA

private fun toggleDualScreenMode(
    activity: ComponentActivity,
    rearScreen: @Composable () -> Unit,
) {
    if (readDisplayWindowAreaSession != null) {
        invokeProtected {
            readDisplayWindowAreaSession?.close()
        }
        readDisplayWindowAreaSession = null
    } else {
        readDisplayWindowAreaInfo?.token?.let { token ->
            invokeProtected {
                readDisplayWindowAreaController.presentContentOnWindowArea(
                    token = token,
                    activity = activity,
                    executor = readDisplayDisplayExecutor,
                    windowAreaPresentationSessionCallback = object :
                        WindowAreaPresentationSessionCallback {
                        override fun onSessionStarted(session: WindowAreaSessionPresenter) {
                            invokeProtected {
                                readDisplayWindowAreaSession = session

                                val layoutInflater =
                                    (session.context as ContextThemeWrapper).getSystemService(
                                        Context.LAYOUT_INFLATER_SERVICE
                                    ) as LayoutInflater

//                                // 1. using View
//                                val view = layoutInflater.inflate(R.layout.activity_rear_view, null, false)
//                                session.setContentView(view)

                                // 2. using Compose
                                val rearComposeView = layoutInflater.inflate(
                                    R.layout.activity_compose,
                                    null,
                                    false
                                ) as ComposeView
                                rearComposeView.apply {
                                    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                                    setViewTreeSavedStateRegistryOwner(activity)
                                    setViewTreeLifecycleOwner(activity)
                                    setContent {
                                        rearScreen()
                                    }
                                }
                                session.setContentView(rearComposeView)
                            }
                        }

                        override fun onContainerVisibilityChanged(isVisible: Boolean) {}

                        override fun onSessionEnded(t: Throwable?) {
                            readDisplayWindowAreaSession = null
                            Log.e("toggleDualScreenMode", "Session ended", t)
                        }
                    }
                )
            }
        }
    }
}

private fun invokeProtected(content: () -> Unit) {
    try {
        content()
    } catch (e: Throwable) {
        Log.e("invokeProtected", "Error invoking content: ${e.message}")
    }
}

private fun FoldingFeature.toFoldableStateInfo(foldableStateType: FoldableStateType): FoldableStateInfo {
    return FoldableStateInfo(
        type = foldableStateType,
        orientation = this.orientation,
        bounds = this.bounds,
        isSeparating = this.isSeparating,
        occlusionType = this.occlusionType,
    )
}

private fun WindowLayoutInfo.toFoldableState(): FoldableState {
    if (this.displayFeatures.isEmpty()) {
        // Not a foldable device or foldable folded
        return FoldableState.None
    }

    return FoldableState.Foldable(
        foldedStateList = displayFeatures.map {
            when ((it as FoldingFeature).state) {
                FoldingFeature.State.FLAT -> it.toFoldableStateInfo(FoldableStateType.Flat)
                FoldingFeature.State.HALF_OPENED -> it.toFoldableStateInfo(FoldableStateType.HalfOpened)
                else -> {
                    null
                }
            }
        }.requireNoNulls()
    )
}

@Preview(showBackground = true, widthDp = 500, heightDp = 500)
@Composable
private fun FoldableContainerWithoutTopBarAndNoPanesPreview() {
    StudyFoldableTheme {
        FoldableContainer(
            foldablePanes = FoldablePanes.None,
            contentMain = {
                PreviewMain(
                    modifier = it,
                    text = "Main content WITHOUT top bar on right pane"
                )
            },
            contentLower = { PreviewContent(modifier = it, text = "Lower pane") },
            contentRight = { PreviewContent(modifier = it, text = "Right pane") },
        )
    }
}

@Preview(showBackground = true, widthDp = 500, heightDp = 500)
@Composable
private fun FoldableContainerWithoutTopBarAndRightPanePreview() {
    StudyFoldableTheme {
        FoldableContainer(
            foldablePanes = FoldablePanes.Right,
            contentMain = {
                PreviewMain(
                    modifier = it,
                    text = "Main content WITHOUT top bar on right pane"
                )
            },
            contentLower = { PreviewContent(modifier = it, text = "Lower pane") },
            contentRight = { PreviewContent(modifier = it, text = "Right pane") },
        )
    }
}

@Preview(showBackground = true, widthDp = 500, heightDp = 500)
@Composable
private fun FoldableContainerWithoutTopBarAndLowerPanePreview() {
    StudyFoldableTheme {
        FoldableContainer(
            foldablePanes = FoldablePanes.Lower,
            contentMain = {
                PreviewMain(
                    modifier = it,
                    text = "Main content WITHOUT top bar on right pane"
                )
            },
            contentLower = { PreviewContent(modifier = it, text = "Lower pane") },
            contentRight = { PreviewContent(modifier = it, text = "Right pane") },
        )
    }
}

@Preview(showBackground = true, widthDp = 500, heightDp = 500)
@Composable
private fun FoldableContainerWithTopBarAndNoPanesPreview() {
    StudyFoldableTheme {
        FoldableContainer(
            foldablePanes = FoldablePanes.None,
            topBar = { PreviewTopBar() },
            contentMain = {
                PreviewContent(
                    modifier = it,
                    text = "Main pane WITH top bar on right pane"
                )
            },
            contentLower = { PreviewContent(modifier = it, text = "Lower pane") },
            contentRight = { PreviewContent(modifier = it, text = "Right pane") },
        )
    }
}

@Preview(showBackground = true, widthDp = 500, heightDp = 500)
@Composable
private fun FoldableContainerWithTopBarAndRightPanePreview() {
    StudyFoldableTheme {
        FoldableContainer(
            foldablePanes = FoldablePanes.Right,
            topBar = { PreviewTopBar() },
            contentMain = {
                PreviewContent(
                    modifier = it,
                    text = "Main pane WITH top bar on right pane"
                )
            },
            contentLower = { PreviewContent(modifier = it, text = "Lower pane") },
            contentRight = { PreviewContent(modifier = it, text = "Right pane") },
        )
    }
}

@Preview(showBackground = true, widthDp = 500, heightDp = 500)
@Composable
private fun FoldableContainerWithTopBarAndLowerPanePreview() {
    StudyFoldableTheme {
        FoldableContainer(
            foldablePanes = FoldablePanes.Lower,
            topBar = { PreviewTopBar() },
            contentMain = {
                PreviewContent(
                    modifier = it,
                    text = "Main pane WITH top bar on right pane"
                )
            },
            contentLower = { PreviewContent(modifier = it, text = "Lower pane") },
            contentRight = { PreviewContent(modifier = it, text = "Right pane") },
        )
    }
}

@Composable
private fun PreviewMain(
    text: String,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            PreviewTopBar()
        },
    ) { innerPadding ->
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PreviewTopBar() {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text("Main")
        },
    )
}

@Composable
private fun PreviewContent(
    text: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
    ) {
        Text(
            text = text,
        )
    }
}