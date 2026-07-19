package com.aistudio.unibuddy.qywvsp.ui.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View.MeasureSpec
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.aistudio.unibuddy.qywvsp.ui.components.BuddyMascot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import androidx.compose.runtime.Recomposer
import androidx.compose.ui.platform.AndroidUiDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume

class FakeSavedStateRegistryOwner : SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    init {
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    override val lifecycle: Lifecycle get() = lifecycleRegistry
}

class FakeViewModelStoreOwner : ViewModelStoreOwner {
    private val store = ViewModelStore()
    override val viewModelStore: ViewModelStore get() = store
}

object PetBitmapRenderer {

    suspend fun generateAndSavePetBitmap(
        context: Context,
        pose: String,
        accessory: String,
        isHappy: Boolean,
        isWorried: Boolean,
        weatherState: String,
        mainColorHex: String
    ): Boolean = withContext(Dispatchers.Main) {
        try {
            val composeView = ComposeView(context).apply {
                setContent {
                    val colorInt = try {
                        android.graphics.Color.parseColor(mainColorHex)
                    } catch (e: Exception) {
                        android.graphics.Color.parseColor("#4CAF50") // Default Green
                    }
                    val color = Color(colorInt)

                    BuddyMascot(
                        modifier = Modifier.size(120.dp),
                        isWorried = isWorried,
                        isHappy = isHappy,
                        pose = pose,
                        accessory = accessory,
                        weatherState = weatherState,
                        mainColor = color
                    )
                }
            }

            val savedStateRegistryOwner = FakeSavedStateRegistryOwner()
            val viewModelStoreOwner = FakeViewModelStoreOwner()

            composeView.setViewTreeLifecycleOwner(savedStateRegistryOwner)
            composeView.setViewTreeSavedStateRegistryOwner(savedStateRegistryOwner)
            composeView.setViewTreeViewModelStoreOwner(viewModelStoreOwner)

            // Create a custom recomposer to handle offscreen composition without being attached to a window
            val recomposer = Recomposer(AndroidUiDispatcher.CurrentThread)
            composeView.setParentCompositionContext(recomposer)
            val recomposerScope = CoroutineScope(AndroidUiDispatcher.CurrentThread)
            val recomposerJob = recomposerScope.launch {
                recomposer.runRecomposeAndApplyChanges()
            }

            // Measure and layout
            val width = (120 * context.resources.displayMetrics.density).toInt()
            val height = (120 * context.resources.displayMetrics.density).toInt()
            composeView.measure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
            )
            composeView.layout(0, 0, width, height)

            // Wait a bit for layout to settle and animation to reach first frame
            suspendCancellableCoroutine { cont ->
                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        composeView.draw(canvas)

                        val isNotEmpty = bitmap.hasNonTransparentPixels()
                        Log.d("PetBitmapRenderer", "Bitmap generated, width: $width, height: $height, hasContent: $isNotEmpty")

                        val file = File(context.filesDir, "widget_pet_current.png")
                        FileOutputStream(file).use { out ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }
                        cont.resume(true)
                    } catch (e: Exception) {
                        Log.e("PetBitmapRenderer", "Error drawing bitmap", e)
                        cont.resume(false)
                    } finally {
                        recomposerJob.cancel()
                        recomposer.cancel()
                        recomposerScope.cancel()
                    }
                }, 150) // Delay to let composition happen
            }
        } catch (e: Exception) {
            Log.e("PetBitmapRenderer", "Error setting up compose view", e)
            false
        }
    }

    private fun Bitmap.hasNonTransparentPixels(): Boolean {
        for (y in 0 until height step 5) {
            for (x in 0 until width step 5) {
                if (getPixel(x, y) != android.graphics.Color.TRANSPARENT) {
                    return true
                }
            }
        }
        return false
    }
}
