package com.example.citymind.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object ImageCompressor {

    fun compressImage(context: Context, imageUri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
            ?: throw IllegalArgumentException("Cannot decode image from URI: $imageUri")
        inputStream?.close()

        val maxDimension = 1920
        var width = originalBitmap.width
        var height = originalBitmap.height

        if (width > maxDimension || height > maxDimension) {
            if (width > height) {
                height = (height * maxDimension.toFloat() / width).toInt()
                width = maxDimension
            } else {
                width = (width * maxDimension.toFloat() / height).toInt()
                height = maxDimension
            }
        }

        val resizedBitmap = if (width != originalBitmap.width || height != originalBitmap.height) {
            Bitmap.createScaledBitmap(originalBitmap, width, height, true)
        } else {
            originalBitmap
        }

        val bytes = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bytes)

        val tempFile = File(context.cacheDir, "citymind_upload_${System.currentTimeMillis()}.jpg")
        val fileOutputStream = FileOutputStream(tempFile)
        fileOutputStream.write(bytes.toByteArray())
        fileOutputStream.flush()
        fileOutputStream.close()

        return tempFile
    }
}
