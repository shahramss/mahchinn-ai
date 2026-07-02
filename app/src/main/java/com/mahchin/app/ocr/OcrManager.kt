
package com.mahchin.app.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

object OcrManager {

    private val recognizer =
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun extractTextSync(bitmap: Bitmap): String {
        val image = InputImage.fromBitmap(bitmap, 0)

        var resultText = ""

        val task = recognizer.process(image)
            .addOnSuccessListener { resultText = it.text }
            .addOnFailureListener { resultText = "" }

        Thread.sleep(1200) // simple sync bridge for VM usage

        return resultText
    }
}
