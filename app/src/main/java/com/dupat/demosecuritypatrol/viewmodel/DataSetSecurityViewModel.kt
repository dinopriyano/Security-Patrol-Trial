package com.dupat.demosecuritypatrol.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dupat.demosecuritypatrol.db.dao.DataSetSecurityDao
import com.dupat.demosecuritypatrol.db.entity.DataSetSecurity
import com.dupat.demosecuritypatrol.network.repositories.SecurityDatabaseRepository
import com.dupat.demosecuritypatrol.utils.Corountines
import com.dupat.demosecuritypatrol.utils.Function.bitmapToByteArray
import com.dupat.demosecuritypatrol.utils.Function.resizedBitmap
import com.dupat.demosecuritypatrol.utils.SingleLiveEvent
import com.dupat.demosecuritypatrol.viewmodel.state.ViewState
import com.google.android.gms.tasks.OnSuccessListener
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.ByteArrayOutputStream
import java.util.*

class DataSetSecurityViewModel(val repository: SecurityDatabaseRepository): ViewModel() {

    private var state: SingleLiveEvent<ViewState> = SingleLiveEvent()
    var bmpImage: Bitmap? = null
    var securityName: String? = null
    var imageUrl: String? = null
    private var dataSet = repository.dataSetSecurity
    private var numValidDetect = MutableLiveData<Int>()

    fun validateImage(){
        state.value = ViewState.IsLoading(true)

        if(bmpImage!!.height < bmpImage!!.width){
            state.value = ViewState.IsLoading(false)
            state.value = ViewState.Error("Image height must be more than width")
        }
        else{
            val options = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setContourMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                .build()
            val detector = FaceDetection.getClient(options)
            val faceDetector: FaceDetector = detector

            val inputImage = InputImage.fromBitmap(bmpImage!!, 0)
            faceDetector
                .process(inputImage)
                .addOnSuccessListener(OnSuccessListener { faces ->
                    if (faces.size == 0) {
                        state.value = ViewState.IsLoading(false)
                        state.value = ViewState.Error("Face not found")
                        Log.d("TAG", "Face not found")
                        return@OnSuccessListener
                    }
                    else if(faces.size > 1){
                        state.value = ViewState.IsLoading(false)
                        state.value = ViewState.Error("Face more than one")
                        Log.d("TAG", "Face more than one")
                        return@OnSuccessListener
                    }
                    else {
                        state.value = ViewState.IsLoading(false)
                        state.value = ViewState.IsSuccess(0)
                        Log.d("TAG", "Face count: " + faces.size)
                    }
                })
        }
    }

    fun addProgress(num: Int){
        numValidDetect.postValue(num)
    }

    fun insertDataSet(){
        if(bmpImage == null){
            state.value = ViewState.Error("Choose image first")
        }
        else{
            state.value = ViewState.IsLoading(true)
            val dataSetSecurity: DataSetSecurity = DataSetSecurity(
                1,
                securityName!!,
                imageUrl!!,
                bitmapToByteArray(bmpImage!!)
            )

            Corountines.main {
                state.value = ViewState.IsLoading(true)
                repository.insert(dataSetSecurity)
                state.value = ViewState.IsLoading(false)
                state.value = ViewState.IsSuccess(1)
            }
        }
    }

    fun getDatSet() = dataSet
    fun getState() = state
    fun getNumValid() = numValidDetect
}