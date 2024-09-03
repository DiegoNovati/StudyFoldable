package com.example.studyfoldable

import android.content.Context
import android.graphics.Rect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.window.layout.FoldingFeature
import androidx.window.layout.FoldingFeature.OcclusionType
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

fun getFoldableState(
    context: Context,
    lifecycleScope: LifecycleCoroutineScope,
    lifecycle: Lifecycle,
    callback: (FoldableState) -> Unit,
) {
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
                else -> { null }
            }
        }.requireNoNulls()
    )
}