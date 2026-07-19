package com.aistudio.unibuddy.qywvsp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.core.app.ApplicationProvider
import com.aistudio.unibuddy.qywvsp.ui.widget.PetBitmapRenderer
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class WidgetRendererTest {

    @Test
    fun testGenerateAndSavePetBitmap() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        // Ejecutar el renderer
        val success = PetBitmapRenderer.generateAndSavePetBitmap(
            context = context,
            pose = "normal",
            accessory = "none",
            isHappy = true,
            isWorried = false,
            weatherState = "clear",
            mainColorHex = "#4CAF50"
        )
        
        assertTrue("Renderer debe devolver true", success)
        
        // Verificar que el archivo se guardó
        val file = File(context.filesDir, "widget_pet_current.png")
        assertTrue("El archivo bitmap debe existir", file.exists())
        
        // Verificar que no sea nulo ni vacío/transparente
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        assertNotNull("El bitmap no debe ser nulo", bitmap)
        assertTrue("El bitmap debe tener un ancho válido", bitmap.width > 0)
        assertTrue("El bitmap debe tener un alto válido", bitmap.height > 0)
        
        // Chequeo de transparencia
        var hasContent = false
        for (y in 0 until bitmap.height step 10) {
            for (x in 0 until bitmap.width step 10) {
                if (bitmap.getPixel(x, y) != android.graphics.Color.TRANSPARENT) {
                    hasContent = true
                    break
                }
            }
            if (hasContent) break
        }
        assertTrue("El bitmap generado no debe ser completamente transparente", hasContent)
    }
}
