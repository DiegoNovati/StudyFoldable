@file:OptIn(ExperimentalWindowApi::class)

package com.example.studyfoldable

import android.content.Context
import android.graphics.Rect
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.repeatOnLifecycle
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

// Ref: https://developer.android.com/guide/topics/large-screens/make-apps-fold-aware

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

@Composable
fun SupportFoldableLowerDisplay(
    context: Context,
    lifecycleScope: LifecycleCoroutineScope,
    lifecycle: Lifecycle,
    showSecondScreenCallback: (Boolean) -> Unit,
) {
    GetFoldableState(
        context = context,
        lifecycleScope = lifecycleScope,
        lifecycle = lifecycle,
    ) { foldableState ->
        if (foldableState is FoldableState.Foldable) {
            val foldableStateInfo = foldableState.foldedStateList.lastOrNull()
            showSecondScreenCallback(
                foldableStateInfo?.type == FoldableStateType.HalfOpened &&
                        foldableStateInfo.orientation == FoldingFeature.Orientation.HORIZONTAL
            )
        }
    }
}

private lateinit var readDisplayWindowAreaController: WindowAreaController
private lateinit var readDisplayDisplayExecutor: Executor
private var readDisplayWindowAreaSession: WindowAreaSession? = null
private var readDisplayWindowAreaInfo: WindowAreaInfo? = null
private val readDisplayOperation = WindowAreaCapability.Operation.OPERATION_PRESENT_ON_AREA

@Composable
fun SupportFoldableRearDisplay(
    activity: ComponentActivity,
    lifecycleScope: LifecycleCoroutineScope,
    lifecycle: Lifecycle,
    capabilityStatusCallback: (WindowAreaCapability.Status) -> Unit,
    secondScreen: @Composable () -> Unit,
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
                        capabilityStatusCallback(capabilityStatus)
                        toggleDualScreenMode(activity, secondScreen)
                    }
            }
        }
    }
}

private fun toggleDualScreenMode(
    activity: ComponentActivity,
    secondScreen: @Composable () -> Unit,
) {
    println("********** toggleDualScreenMode: windowAreaSession = $readDisplayWindowAreaSession")
    if (readDisplayWindowAreaSession != null) {
        readDisplayWindowAreaSession?.close()
    } else {
        readDisplayWindowAreaInfo?.token?.let { token ->
            readDisplayWindowAreaController.presentContentOnWindowArea(
                token = token,
                activity = activity,
                executor = readDisplayDisplayExecutor,
                windowAreaPresentationSessionCallback = object :
                    WindowAreaPresentationSessionCallback {
                    override fun onSessionStarted(session: WindowAreaSessionPresenter) {
                        println("********** onSessionStarted: session = $session")
                        println("********** onSessionStarted: context = ${session.context}")
                        readDisplayWindowAreaSession = session

                        // !!!!!!!!!!! Replace !!!!!!!!!!
                        val view = TextView(session.context)
                        view.text = "Hello world!"
                        session.setContentView(view)

//                        session.setContentView(LinearLayout(session.context).apply {
//                            setViewTreeLifecycleOwner(activity)
//                            layoutParams = LinearLayout.LayoutParams(
//                                LinearLayout.LayoutParams.MATCH_PARENT,
//                                LinearLayout.LayoutParams.MATCH_PARENT
//                            )
//                            orientation = LinearLayout.VERTICAL
//                            addView(ComposeView(session.context).apply {
//                                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
//                                setContent {
//                                    secondScreen()
//                                }
//                            })
//                        })

//                        val linearLayout = LinearLayout(session.context)
//                        linearLayout.setViewTreeLifecycleOwner(activity)
//                        linearLayout.layoutParams = LinearLayout.LayoutParams(
//                            LinearLayout.LayoutParams.MATCH_PARENT,
//                            LinearLayout.LayoutParams.MATCH_PARENT
//                        )
//                        linearLayout.orientation = LinearLayout.VERTICAL
//                        val composeView = ComposeView(session.context)
//                        //composeView.setViewTreeLifecycleOwner(this)
//                        composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
//                        composeView.setContent {
//                            ScreenSecond()
//                        }
//                        linearLayout.addView(composeView)
//                        session.setContentView(linearLayout)

////        val composeView = ComposeView(session.context)
////        composeView.setViewTreeLifecycleOwner(this)
////        composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
////        composeView.setContent {
////            ScreenSecond()
////        }
////        session.setContentView(composeView)
//
////        val view = LinearLayout(session.context)
////        session.setContentView(view)
////        val composeView = ComposeView(view.context)
////        composeView.setViewTreeLifecycleOwner(view.findViewTreeLifecycleOwner())
////        composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
////        composeView.setContent {
////            ScreenSecond()
////        }
////        view.addView(composeView)
                    }

                    override fun onContainerVisibilityChanged(isVisible: Boolean) {
                        println("********** onContainerVisibilityChanged = $isVisible")
                    }

                    override fun onSessionEnded(t: Throwable?) {
                        println("********** onSessionEnded = $t")
                    }
                }
            )
        }
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