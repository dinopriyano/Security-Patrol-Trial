/* Copyright 2019 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/
package com.dupat.demosecuritypatrol.facerecognition.tflite

import android.graphics.Bitmap
import android.graphics.RectF

interface SimilarityClassifier {
    fun register(name: String?, recognition: Recognition?)
    fun recognizeImage(bitmap: Bitmap?, getExtra: Boolean): List<Recognition?>?

    fun enableStatLogging(debug: Boolean)
    val statString: String?
    fun close()
    fun setNumThreads(num_threads: Int)
    fun setUseNNAPI(isChecked: Boolean)

    class Recognition(
        val id: String?,
        val title: String?,
        val distance: Float?,
        private var location: RectF?
    ) {

        var extra: Any? = null

        var color: Int? = null
        var crop: Bitmap? = null

        fun getLocation(): RectF {
            return RectF(location)
        }

        fun setLocation(location: RectF?) {
            this.location = location
        }

        override fun toString(): String {
            var resultString = ""
            if (id != null) {
                resultString += "[$id] "
            }
            if (title != null) {
                resultString += "$title "
            }
            if (distance != null) {
                resultString += String.format("(%.1f%%) ", distance * 100.0f)
            }
            if (location != null) {
                resultString += location.toString() + " "
            }
            return resultString.trim { it <= ' ' }
        }

    }
}