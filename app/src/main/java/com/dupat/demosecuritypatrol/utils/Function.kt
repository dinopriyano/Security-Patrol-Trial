package com.dupat.demosecuritypatrol.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.ByteArrayOutputStream


object Function {

    fun bitmapToByteArray(bmp: Bitmap): ByteArray{
        val stream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray: ByteArray = stream.toByteArray()
        bmp.recycle()
        return byteArray
    }

    fun byteArrayToBitmap(arr: ByteArray): Bitmap{
        val bitmap = BitmapFactory.decodeByteArray(arr, 0, arr.size)
        return bitmap.copy(Bitmap.Config.ARGB_8888, true);
    }

    fun resizedBitmap(bm: Bitmap, newHeight: Int): Bitmap? {
        val width = bm.width
        val height = bm.height
        val scale: Float = width.toFloat()/height.toFloat()
        val newWidth = newHeight * scale
        Log.d("Ukuran","$width $height $scale $newWidth $newHeight")

        // "RECREATE" THE NEW BITMAP
        return Bitmap.createScaledBitmap(bm,newWidth.toInt(), newHeight,false)
    }
}