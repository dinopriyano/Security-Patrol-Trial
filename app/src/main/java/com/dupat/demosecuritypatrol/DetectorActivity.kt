package com.dupat.demosecuritypatrol

import android.content.Intent
import android.graphics.*
import android.hardware.camera2.CameraCharacteristics
import android.media.ImageReader.OnImageAvailableListener
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import com.dupat.demosecuritypatrol.facerecognition.customview.OverlayView
import com.dupat.demosecuritypatrol.facerecognition.customview.OverlayView.DrawCallback
import com.dupat.demosecuritypatrol.facerecognition.env.BorderedText
import com.dupat.demosecuritypatrol.facerecognition.env.ImageUtils
import com.dupat.demosecuritypatrol.facerecognition.env.Logger
import com.dupat.demosecuritypatrol.facerecognition.tflite.SimilarityClassifier
import com.dupat.demosecuritypatrol.facerecognition.tflite.SimilarityClassifier.Recognition
import com.dupat.demosecuritypatrol.facerecognition.tflite.TFLiteObjectDetectionAPIModel
import com.dupat.demosecuritypatrol.facerecognition.tracking.MultiBoxTracker
import com.dupat.demosecuritypatrol.session.SharedPrefManager
import com.dupat.demosecuritypatrol.utils.Function.byteArrayToBitmap
import com.dupat.demosecuritypatrol.utils.Function.resizedBitmap
import com.dupat.demosecuritypatrol.utils.snackbar
import com.dupat.demosecuritypatrol.utils.toast
import com.dupat.demosecuritypatrol.viewmodel.state.ViewState
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.android.synthetic.main.tfe_od_activity_camera.*
import kotlinx.android.synthetic.main.tfe_od_camera_connection_fragment_tracking.*
import kotlinx.android.synthetic.main.tfe_od_layout_bottom_sheet.*
import java.util.*

open class DetectorActivity : CameraActivity(), OnImageAvailableListener {
    var trackingOverlay: OverlayView? = null
    private var sensorOrientation: Int? = null
    lateinit var detector: SimilarityClassifier
    private var lastProcessingTimeMs: Long = 0
    private var rgbFrameBitmap: Bitmap? = null
    private var croppedBitmap: Bitmap? = null
    lateinit var cropCopyBitmap: Bitmap
    private var computingDetection = false
    private var addPending = false

    //private boolean adding = false;
    private var timestamp: Long = 0
    private var frameToCropTransform: Matrix? = null
    private var cropToFrameTransform: Matrix? = null

    //private Matrix cropToPortraitTransform;
    private var tracker: MultiBoxTracker? = null
    private var borderedText: BorderedText? = null

    // Face detector
    private var faceDetector: FaceDetector? = null

    // here the preview image is drawn in portrait way
    private var portraitBmp: Bitmap? = null

    // here the face is cropped and drawn
    private var faceBmp: Bitmap? = null
    lateinit var fabAdd: FloatingActionButton

    //bitmap user
    private var bmpUser: Bitmap? = null
    private var nameUser: String? = null
    private var isResume: Boolean = false
    private var bmpOriginal: Bitmap? = null
    private var numValidDetect: Int = 0
    private var numCountValid: Int = 0
    private var userLat: String? = null
    private var userLong: String? = null
    private var zone_id: String? = null
    private var status: String? = null
    private var note: String? = null
    private var image_situation_path: String? = null

    //private HashMap<String, Classifier.Recognition> knownFaces = new HashMap<>();
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleUIState()
        handleProgressValidFace()

        fabAdd = findViewById(R.id.fab_add)
        fabAdd.setOnClickListener(View.OnClickListener { onAddClick() })

        // Real-time contour detection of multiple faces
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setContourMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .build()
        val detector = FaceDetection.getClient(options)
        faceDetector = detector

        userLat = intent.getStringExtra("lat")
        userLong = intent.getStringExtra("long")
        zone_id = intent.getStringExtra("zoneID")
        status = intent.getStringExtra("status")
        note = intent.getStringExtra("note")
        image_situation_path = intent.getStringExtra("imgSituation")

        Log.d("Data", "guys: $userLong $userLat $zone_id $status $note $image_situation_path")

        //checkWritePermission();
    }

    private fun handleProgressValidFace() {
        viewModel.getNumValid().observe(this, androidx.lifecycle.Observer {
            val progress = it * 4
            progressData.progress = progress.toFloat()

            if(it == 25){
                viewModel.latitude = userLat
                viewModel.longitude = userLong
                viewModel.note = note
                viewModel.photo = image_situation_path
                viewModel.status = status
                viewModel.user_id = SharedPrefManager.getString(this,"id")
                viewModel.zone_id = zone_id
                viewModel.AddingReport()
            }
        })
    }

    private fun handleUIState() {
        viewModel.getState().observer(this, androidx.lifecycle.Observer {
            when (it) {
                is ViewState.IsLoading -> {
                    containerCamera.snackbar("Loading...")
                }
                is ViewState.Error -> {
                    containerCamera.snackbar(it.err!!)
                }
                is ViewState.IsSuccess -> {
                    when (it.what) {
                        1 -> {
                            firstTackFace()
                        }
                        3 -> {
                            val intent = Intent(this,HomeActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent)
                            overridePendingTransition(R.anim.slide_in_right,R.anim.stay)
                            toast("Berhasil menambahkan laporan!")
                        }
                    }
                }
            }
        })
    }

    private fun saveDataSet() {
        Log.d("Save", "saveDataSet gan")
        viewModel.insertDataSet()
    }

    private fun onAddClick() {
        addPending = true
    }

    private fun firstTackFace() {
        if (bmpUser == null) {
            viewModel.getDatSet().observe(this, androidx.lifecycle.Observer {
//                toast(th.size.toString())
                val data = it[0]
                bmpUser = byteArrayToBitmap(data.imageData)
                nameUser = data.imageName
                firstTackFace()
            })
        } else {

            isResume = true
            addPending = true

            val image = InputImage.fromBitmap(bmpUser!!, 0)
            faceDetector!!
                .process(image)
                .addOnSuccessListener(OnSuccessListener { faces ->
                    if (faces.size == 0) {
                        updateResults(timestamp, LinkedList())
                        return@OnSuccessListener
                    }
                    runInBackground(
                        Runnable {
                            onFacesDetected(timestamp, faces, addPending)
//                            portraitBmp = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888)
//                            isResume = false
                            addPending = false
                        })
                })


        }
    }

    public override fun onPreviewSizeChosen(size: Size?, rotation: Int) {
        if (isFirstLogin) {
            if (intent.hasExtra("isFirst")) {
                bmpOriginal = MediaStore.Images.Media.getBitmap(
                    contentResolver,
                    intent.getParcelableExtra("bmpUri")
                )
                if (bmpOriginal!!.height > (size!!.height / 2)) {
                    bmpOriginal = resizedBitmap(bmpOriginal!!, (size.height / 2))
                }
                viewModel.bmpImage = bmpOriginal
                viewModel.imageUrl = "https://google.com"
                viewModel.securityName = intent.getStringExtra("securityName")
                saveDataSet()
            }
            isFirstLogin = false
        }

        val textSizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            TEXT_SIZE_DIP,
            resources.displayMetrics
        )
        borderedText = BorderedText(textSizePx)
        borderedText!!.setTypeface(Typeface.MONOSPACE)
        tracker = MultiBoxTracker(this)
        try {
            detector = TFLiteObjectDetectionAPIModel.create(
                assets,
                TF_OD_API_MODEL_FILE,
                TF_OD_API_LABELS_FILE,
                TF_OD_API_INPUT_SIZE,
                TF_OD_API_IS_QUANTIZED
            )
            //cropSize = TF_OD_API_INPUT_SIZE;
        } catch (e: Exception) {
            e.printStackTrace()
            LOGGER.e(e, "Exception initializing classifier!")
            val toast = Toast.makeText(
                applicationContext, "Classifier could not be initialized", Toast.LENGTH_SHORT
            )
            toast.show()
            finish()
        }

        previewWidth = size!!.width
        previewHeight = size.height
        sensorOrientation = rotation - screenOrientation
        LOGGER.i(
            "Camera orientation relative to screen canvas: %d",
            sensorOrientation
        )
        LOGGER.i(
            "Initializing at size %dx%d",
            previewWidth,
            previewHeight
        )
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888)
        val targetW: Int
        val targetH: Int
        if (sensorOrientation == 90 || sensorOrientation == 270) {
            targetH = previewWidth
            targetW = previewHeight
        } else {
            targetW = previewWidth
            targetH = previewHeight
        }
        val cropW = (targetW / 2.0).toInt()
        val cropH = (targetH / 2.0).toInt()
        croppedBitmap = Bitmap.createBitmap(cropW, cropH, Bitmap.Config.ARGB_8888)
        portraitBmp = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
        faceBmp = Bitmap.createBitmap(
            TF_OD_API_INPUT_SIZE,
            TF_OD_API_INPUT_SIZE,
            Bitmap.Config.ARGB_8888
        )
        frameToCropTransform =
            ImageUtils.getTransformationMatrix(
                previewWidth, previewHeight,
                cropW, cropH,
                sensorOrientation!!, MAINTAIN_ASPECT
            )

//    frameToCropTransform =
//            ImageUtils.getTransformationMatrix(
//                    previewWidth, previewHeight,
//                    previewWidth, previewHeight,
//                    sensorOrientation, MAINTAIN_ASPECT);
        cropToFrameTransform = Matrix()
        frameToCropTransform!!.invert(cropToFrameTransform)
        val frameToPortraitTransform =
            ImageUtils.getTransformationMatrix(
                previewWidth, previewHeight,
                targetW, targetH,
                sensorOrientation!!, MAINTAIN_ASPECT
            )
        trackingOverlay = findViewById<View>(R.id.tracking_overlay) as OverlayView
        trackingOverlay!!.addCallback(
            object : DrawCallback {
                override fun drawCallback(canvas: Canvas?) {
                    tracker!!.draw(canvas!!)
                    if (isDebug) {
                        tracker!!.drawDebug(canvas)
                    }
                }
            })
        tracker!!.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation!!)
    }

    override fun processImage() {
        ++timestamp
        val currTimestamp = timestamp
        trackingOverlay!!.postInvalidate()

        // No mutex needed as this method is not reentrant.
        if (computingDetection) {
            readyForNextImage()
            return
        }
        computingDetection = true
        LOGGER.i("Preparing image $currTimestamp for detection in bg thread.")
        rgbFrameBitmap!!.setPixels(
            getRgbBytes(),
            0,
            previewWidth,
            0,
            0,
            previewWidth,
            previewHeight
        )
        readyForNextImage()
        val canvas = Canvas(croppedBitmap!!)
        canvas.drawBitmap(rgbFrameBitmap!!, frameToCropTransform!!, null)
        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(faceBmp!!)
        }
        val image = InputImage.fromBitmap(croppedBitmap!!, 0)
        faceDetector!!
            .process(image)
            .addOnSuccessListener(OnSuccessListener { faces ->
                if (faces.size == 0) {
                    updateResults(currTimestamp, LinkedList())
                    return@OnSuccessListener
                }
                runInBackground(
                    Runnable {
                        if(!isResume){
                            onFacesDetected(currTimestamp, faces, addPending)
                            addPending = false
                        }
                    })
            })
    }

    override val layoutId: Int
        get() = R.layout.tfe_od_camera_connection_fragment_tracking
    override val desiredPreviewFrameSize: Size?
        get() = Size(640, 480)

    // Which detection model to use: by default uses Tensorflow Object Detection API frozen
    // checkpoints.
    private enum class DetectorMode {
        TF_OD_API
    }

    override fun setUseNNAPI(isChecked: Boolean) {
        runInBackground(Runnable { detector!!.setUseNNAPI(isChecked) })
    }

    override fun setNumThreads(numThreads: Int) {
        runInBackground(Runnable { detector!!.setNumThreads(numThreads) })
    }

    // Face Processing
    private fun createTransform(
        srcWidth: Int,
        srcHeight: Int,
        dstWidth: Int,
        dstHeight: Int,
        applyRotation: Int
    ): Matrix {
        val matrix = Matrix()
        if (applyRotation != 0) {
            if (applyRotation % 90 != 0) {
                LOGGER.w("Rotation of %d % 90 != 0", applyRotation)
            }

            // Translate so center of image is at origin.
            matrix.postTranslate(-srcWidth / 2.0f, -srcHeight / 2.0f)

            // Rotate around origin.
            matrix.postRotate(applyRotation.toFloat())
        }

//        // Account for the already applied rotation, if any, and then determine how
//        // much scaling is needed for each axis.
//        final boolean transpose = (Math.abs(applyRotation) + 90) % 180 == 0;
//        final int inWidth = transpose ? srcHeight : srcWidth;
//        final int inHeight = transpose ? srcWidth : srcHeight;
        if (applyRotation != 0) {

            // Translate back from origin centered reference to destination frame.
            matrix.postTranslate(dstWidth / 2.0f, dstHeight / 2.0f)
        }
        return matrix
    }

    private fun showAddFaceDialog(rec: Recognition) {
//        val builder = AlertDialog.Builder(this)
//        val inflater = layoutInflater
//        val dialogLayout = inflater.inflate(R.layout.image_edit_dialog, null)
//        val ivFace =
//            dialogLayout.findViewById<ImageView>(R.id.dlg_image)
//        val tvTitle = dialogLayout.findViewById<TextView>(R.id.dlg_title)
//        val etName = dialogLayout.findViewById<EditText>(R.id.dlg_input)
//        tvTitle.text = "Add Face"
//
//        Log.d("Kontci", "${rec.crop?.width} ${rec.crop?.height}")
//
//        ivFace.setImageBitmap(rec.crop)
//        etName.hint = "Input name"
//        builder.setPositiveButton("OK", DialogInterface.OnClickListener { dlg, i ->
//            val name = etName.text.toString()
//            if (name.isEmpty()) {
//                return@OnClickListener
//            }
//            detector!!.register(name, rec)
//            //knownFaces.put(name, rec);
//            dlg.dismiss()
//        })
//        builder.setView(dialogLayout)
//        builder.show()
        Log.d("Adding", "Adding face: ${rec.title}")
        detector!!.register(nameUser, rec)
    }

    private fun updateResults(currTimestamp: Long, mappedRecognitions: List<Recognition>) {
        tracker!!.trackResults(mappedRecognitions, currTimestamp)
        trackingOverlay!!.postInvalidate()
        computingDetection = false
        //adding = false;
        if (mappedRecognitions.isNotEmpty()) {
            LOGGER.i("Adding results")
            val rec = mappedRecognitions[0]
            if (rec.extra != null) {
                showAddFaceDialog(rec)
            }
        }
        runOnUiThread {
            showFrameInfo("$previewWidth x $previewHeight")
            showCropInfo(
                croppedBitmap!!.width.toString() + " x " + croppedBitmap!!.height
            )
            showInference(lastProcessingTimeMs.toString() + " ms")
        }
    }

    override fun onResume() {
        super.onResume()
        numCountValid = 0
        if (!isFirstLogin) {
            firstTackFace()
        }
    }

    private fun onFacesDetected(currTimestamp: Long, faces: List<Face>, add: Boolean) {
        cropCopyBitmap = Bitmap.createBitmap(croppedBitmap!!)
        val canvas = Canvas(cropCopyBitmap)
        val paint = Paint()
        paint.color = Color.RED
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.0f
        var minimumConfidence =
            MINIMUM_CONFIDENCE_TF_OD_API
        minimumConfidence = when (MODE) {
            DetectorMode.TF_OD_API -> MINIMUM_CONFIDENCE_TF_OD_API
        }
        val mappedRecognitions: MutableList<Recognition> = LinkedList()


        //final List<Classifier.Recognition> results = new ArrayList<>();

        // Note this can be done only once
        val sourceW = rgbFrameBitmap!!.width
        val sourceH = rgbFrameBitmap!!.height
        val targetW = portraitBmp!!.width
        val targetH = portraitBmp!!.height
        val transform = createTransform(
            sourceW,
            sourceH,
            targetW,
            targetH,
            sensorOrientation!!
        )
        val cv = Canvas(portraitBmp!!)

        // draws the original image in portrait mode.
        cv.drawBitmap(rgbFrameBitmap!!, transform, null)
        val cvFace = Canvas(faceBmp!!)
        val saved = false
        if (faces.isEmpty() || faces.size > 1) {
            numCountValid = 0
        }
        for (face in faces) {
            LOGGER.i("FACE $face")
            LOGGER.i("Running detection on face $currTimestamp")
            //results = detector.recognizeImage(croppedBitmap);
            val boundingBox = RectF(face.boundingBox)
            Log.d("TAG", "Bounding Box: $boundingBox")

            //final boolean goodConfidence = result.getConfidence() >= minimumConfidence;
            val goodConfidence = true //face.get;
            if (boundingBox != null && goodConfidence) {

                // maps crop coordinates to original
                cropToFrameTransform!!.mapRect(boundingBox)

                // maps original coordinates to portrait coordinates
                val faceBB = RectF(boundingBox)
                transform.mapRect(faceBB)

                // translates portrait to origin and scales to fit input inference size
                //cv.drawRect(faceBB, paint);
                val sx = TF_OD_API_INPUT_SIZE.toFloat() / faceBB.width()
                val sy = TF_OD_API_INPUT_SIZE.toFloat() / faceBB.height()
                val matrix = Matrix()
                matrix.postTranslate(-faceBB.left, -faceBB.top)
                matrix.postScale(sx, sy)
                cvFace.drawBitmap(portraitBmp!!, matrix, null)

                //canvas.drawRect(faceBB, paint);
                var label = ""
                var confidence = -1f
                var color = Color.BLUE
                var extra: Any? = null
                var crop: Bitmap? = null
                if (add) {
                    if (isResume) {
                        crop = Bitmap.createBitmap(
                            bmpUser!!,
                            faceBB.left.toInt() / 2,
                            faceBB.top.toInt() / 2,
                            faceBB.width().toInt() / 2,
                            faceBB.height().toInt() / 2
                        )
                    } else {
                        crop = Bitmap.createBitmap(
                            portraitBmp!!,
                            faceBB.left.toInt(),
                            faceBB.top.toInt(),
                            faceBB.width().toInt(),
                            faceBB.height().toInt()
                        )
                    }
                }
                val startTime = SystemClock.uptimeMillis()
                val resultsAux = detector.recognizeImage(
                    if (add) {
                        Bitmap.createScaledBitmap(
                            crop!!,
                            TF_OD_API_INPUT_SIZE,
                            TF_OD_API_INPUT_SIZE,
                            false
                        )
                    } else faceBmp, add
                )
                lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime
                if (resultsAux!!.isNotEmpty()) {
                    val result = resultsAux[0]
                    extra = result!!.extra
                    Log.d("TAG", "extra gan: $extra")
                    //          Object extra = result.getExtra();
//          if (extra != null) {
//            LOGGER.i("embeeding retrieved " + extra.toString());
//          }
                    val conf = result.distance!!
                    if (conf < 1.0f) {
                        confidence = conf
                        label = result.title!!
                        LOGGER.i("Mukanya $label")
                        color = if (result.id == "0") {
                            Color.GREEN
                        } else {
                            Color.RED
                        }
                    }
                }
                if (cameraFacing == CameraCharacteristics.LENS_FACING_FRONT) {

                    // camera is frontal so the image is flipped horizontally
                    // flips horizontally
                    val flip = Matrix()
                    if (sensorOrientation == 90 || sensorOrientation == 270) {
                        flip.postScale(1f, -1f, previewWidth / 2.0f, previewHeight / 2.0f)
                    } else {
                        flip.postScale(-1f, 1f, previewWidth / 2.0f, previewHeight / 2.0f)
                    }
                    //flip.postScale(1, -1, targetW / 2.0f, targetH / 2.0f);
                    flip.mapRect(boundingBox)
                }

                //Result here
                val result = Recognition(
                    "0", label, confidence, boundingBox
                )
                result.color = color
                result.setLocation(boundingBox)
                result.extra = extra
                result.crop = crop
                mappedRecognitions.add(result)

                if (result.title == nameUser) {
                    numCountValid++
                } else {
                    numCountValid = 0
                }

                if (numCountValid in (numValidDetect + 1)..25) {
                    numValidDetect = numCountValid
                    viewModel.addProgress(numValidDetect)
//                    toast(progress.toString())
                }
            }
        }

        //    if (saved) {
//      lastSaved = System.currentTimeMillis();
//    }
        updateResults(currTimestamp, mappedRecognitions)
        if(isResume)
            isResume = false
    }

    companion object {
        private val LOGGER = Logger()

        // FaceNet
        //  private static final int TF_OD_API_INPUT_SIZE = 160;
        //  private static final boolean TF_OD_API_IS_QUANTIZED = false;
        //  private static final String TF_OD_API_MODEL_FILE = "facenet.tflite";
        //  //private static final String TF_OD_API_MODEL_FILE = "facenet_hiroki.tflite";
        // MobileFaceNet
        private const val TF_OD_API_INPUT_SIZE = 112
        private const val TF_OD_API_IS_QUANTIZED = false
        private const val TF_OD_API_MODEL_FILE = "mobile_face_net.tflite"
        private const val TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt"
        private val MODE = DetectorMode.TF_OD_API

        // Minimum detection confidence to track a detection.
        private const val MINIMUM_CONFIDENCE_TF_OD_API = 0.5f
        private const val MAINTAIN_ASPECT = false

        //private static final int CROP_SIZE = 320;
        //private static final Size CROP_SIZE = new Size(320, 320);
        private const val SAVE_PREVIEW_BITMAP = false
        private const val TEXT_SIZE_DIP = 10f
    }
}