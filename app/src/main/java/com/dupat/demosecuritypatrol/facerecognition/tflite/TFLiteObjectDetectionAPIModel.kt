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

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Trace
import android.util.Log
import android.util.Pair
import com.dupat.demosecuritypatrol.facerecognition.env.Logger
import com.dupat.demosecuritypatrol.facerecognition.tflite.SimilarityClassifier.Recognition
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*

class TFLiteObjectDetectionAPIModel private constructor() : SimilarityClassifier {
    private var isModelQuantized = false

    private var inputSize = 0

    private val labels = Vector<String>()
    lateinit var intValues: IntArray

    lateinit var outputLocations: Array<Array<FloatArray>>

    lateinit var outputClasses: Array<FloatArray>

    lateinit var outputScores: Array<FloatArray>

    lateinit var numDetections: FloatArray
    lateinit var embeedings: Array<FloatArray>
    private var imgData: ByteBuffer? = null
    private var tfLite: Interpreter? = null

    lateinit var output: Array<FloatArray>
    private val registered = HashMap<String?, Recognition?>()

    override fun register(name: String?, rec: Recognition?) {
        registered[name] = rec
        Log.d("Reg", "register gan")
    }

    private fun findNearest(emb: FloatArray): Pair<String?, Float>? {
        var ret: Pair<String?, Float>? = null
        for ((name, value) in registered) {
            val knownEmb =
                (value!!.extra as Array<FloatArray>?)!![0]
            var distance = 0f
            for (i in emb.indices) {
                val diff = emb[i] - knownEmb[i]
                distance += diff * diff
            }
            distance = Math.sqrt(distance.toDouble()).toFloat()
            if (ret == null || distance < ret.second) {
                ret = Pair(name, distance)
            }
        }
        return ret
    }

    override fun recognizeImage(bitmap: Bitmap?, storeExtra: Boolean): List<Recognition?> {
        Trace.beginSection("recognizeImage")
        Trace.beginSection("preprocessBitmap")
        bitmap!!.getPixels(
            intValues,
            0,
            bitmap.width,
            0,
            0,
            bitmap.width,
            bitmap.height
        )
        imgData!!.rewind()
        for (i in 0 until inputSize) {
            for (j in 0 until inputSize) {
                val pixelValue = intValues[i * inputSize + j]
                if (isModelQuantized) {
                    imgData!!.put((pixelValue shr 16 and 0xFF).toByte())
                    imgData!!.put((pixelValue shr 8 and 0xFF).toByte())
                    imgData!!.put((pixelValue and 0xFF).toByte())
                } else {
                    imgData!!.putFloat(((pixelValue shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                    imgData!!.putFloat(((pixelValue shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                    imgData!!.putFloat(((pixelValue and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                }
            }
        }
        Trace.endSection()

        Trace.beginSection("feed")
        val inputArray = arrayOf<Any?>(imgData)
        Trace.endSection()

        val outputMap: MutableMap<Int, Any> =
            HashMap()
        embeedings = Array(1) { FloatArray(OUTPUT_SIZE) }
        outputMap[0] = embeedings

        Trace.beginSection("run")
        tfLite!!.runForMultipleInputsOutputs(inputArray, outputMap)
        Trace.endSection()

        var distance = Float.MAX_VALUE
        val id = "0"
        var label: String? = "?"
        if (registered.size > 0) {
            val nearest =
                findNearest(embeedings[0])
            if (nearest != null) {
                val name = nearest.first
                label = name
                distance = nearest.second
                LOGGER.i("nearest: $name - distance: $distance")
            }
        }
        val numDetectionsOutput = 1
        val recognitions =
            ArrayList<Recognition?>(numDetectionsOutput)
        val rec = Recognition(
            id,
            label,
            distance,
            RectF()
        )
        recognitions.add(rec)
        if (storeExtra) {
            rec.extra = embeedings
        }
        Trace.endSection()
        return recognitions
    }

    override fun enableStatLogging(logStats: Boolean) {}
    override val statString: String
        get() = ""

    override fun close() {}
    override fun setNumThreads(num_threads: Int) {
        if (tfLite != null) tfLite!!.setNumThreads(num_threads)
    }

    override fun setUseNNAPI(isChecked: Boolean) {
        if (tfLite != null) tfLite!!.setUseNNAPI(isChecked)
    }

    companion object {
        private val LOGGER =
            Logger()

        private const val OUTPUT_SIZE = 192

        private const val NUM_DETECTIONS = 1

        private const val IMAGE_MEAN = 128.0f
        private const val IMAGE_STD = 128.0f

        private const val NUM_THREADS = 4

        @Throws(IOException::class)
        private fun loadModelFile(
            assets: AssetManager,
            modelFilename: String
        ): MappedByteBuffer {
            val fileDescriptor = assets.openFd(modelFilename)
            val inputStream =
                FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            return fileChannel.map(
                FileChannel.MapMode.READ_ONLY,
                startOffset,
                declaredLength
            )
        }

        @Throws(IOException::class)
        fun create(
            assetManager: AssetManager,
            modelFilename: String,
            labelFilename: String,
            inputSize: Int,
            isQuantized: Boolean
        ): SimilarityClassifier {
            var d = TFLiteObjectDetectionAPIModel()
            val actualFilename = labelFilename.split("file:///android_asset/")[1]
            val labelsInput = assetManager.open(actualFilename)
            val br = BufferedReader(InputStreamReader(labelsInput))

            var line: String?
            while (br.readLine().also { din ->
                    line = din } != null) {
                LOGGER.w(line!!)
                d.labels.add(line)
            }
            br.close()
            d.inputSize = inputSize
            try {
                d.tfLite = Interpreter(
                    loadModelFile(
                        assetManager,
                        modelFilename
                    )
                )
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
            d.isModelQuantized = isQuantized
            val numBytesPerChannel: Int = if (isQuantized) {
                1
            } else {
                4
            }
            d.imgData =
                ByteBuffer.allocateDirect(1 * d.inputSize * d.inputSize * 3 * numBytesPerChannel)
            d.imgData!!.order(ByteOrder.nativeOrder())
            d.intValues = IntArray(d.inputSize * d.inputSize)
            d.tfLite!!.setNumThreads(NUM_THREADS)
            d.outputLocations = Array(
                1
            ) {
                Array(
                    NUM_DETECTIONS
                ) { FloatArray(4) }
            }
            d.outputClasses = Array(
                1
            ) { FloatArray(NUM_DETECTIONS) }
            d.outputScores = Array(
                1
            ) { FloatArray(NUM_DETECTIONS) }
            d.numDetections = FloatArray(1)
            return d
        }
    }
}