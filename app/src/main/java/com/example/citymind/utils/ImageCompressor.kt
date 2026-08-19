package com.example.citymind.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageCompressor {

    private const val TAG = "ImageCompressor"

    /**
     * Compresses an image from a URI and returns a temporary file.
     * Performs all operations on Dispatchers.IO to prevent ANR.
     */
    suspend fun compressImage(context: Context, imageUri: Uri): File = withContext(Dispatchers.IO) {
        var inputStream: InputStream? = null
        try {
            Log.d(TAG, "Opening stream for URI: $imageUri")
            inputStream = context.contentResolver.openInputStream(imageUri)
            
            // 1. Decode dimensions only to avoid loading full image into memory
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // 2. Calculate sample size (power of 2)
            val maxDimension = 1280
            options.inSampleSize = calculateInSampleSize(options, maxDimension, maxDimension)
            options.inJustDecodeBounds = false

            // 3. Decode actual bitmap with sample size
            inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
                ?: throw IllegalArgumentException("Cannot decode image from URI: $imageUri")
            inputStream?.close()

            // 4. Resize to exact dimensions if still too large
            val finalBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                val scale = maxDimension.toFloat() / Math.max(bitmap.width, bitmap.height)
                val scaled = Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * scale).toInt(),
                    (bitmap.height * scale).toInt(),
                    true
                )
                if (scaled != bitmap) {
                    bitmap.recycle() // Recycle original if a new one was created
                }
                scaled
            } else {
                bitmap
            }

            // 5. Save and compress directly to file (avoiding large intermediate byte arrays)
            val tempFile = File(context.cacheDir, "citymind_upload_${System.currentTimeMillis()}.jpg")
            FileOutputStream(tempFile).use { out ->
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 70, out)
                out.flush()
            }
            
            finalBitmap.recycle()

            Log.d(TAG, "Compressed image ready: ${tempFile.absolutePath} (${tempFile.length() / 1024} KB)")
            tempFile
        } catch (t: Throwable) {
            Log.e(TAG, "Critical error during image compression", t)
            inputStream?.close()
            throw t
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
