package com.dupat.demosecuritypatrol

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import androidx.appcompat.view.SupportMenuInflater
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.dupat.demosecuritypatrol.databinding.ActivityDetailLocationBinding
import com.dupat.demosecuritypatrol.network.response.WebResponse
import com.dupat.demosecuritypatrol.network.response.data.LocationData
import com.dupat.demosecuritypatrol.session.SharedPrefManager
import com.dupat.demosecuritypatrol.utils.GetProperImageRotation
import com.dupat.demosecuritypatrol.utils.snackbar
import com.dupat.demosecuritypatrol.utils.toast
import com.dupat.demosecuritypatrol.viewmodel.DetailLocationViewModel
import com.dupat.demosecuritypatrol.viewmodel.state.ViewState
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.mindorks.paracamera.Camera
import im.delight.android.location.SimpleLocation
import kotlinx.android.synthetic.main.activity_detail_location.*
import kotlinx.android.synthetic.main.activity_detail_location.etName
import kotlinx.android.synthetic.main.activity_detail_location.toolbar
import org.json.JSONObject

class DetailLocationActivity : AppCompatActivity(),PermissionListener,View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    lateinit var location: SimpleLocation
    lateinit var viewmodel: DetailLocationViewModel
    lateinit var binding: ActivityDetailLocationBinding
    lateinit var camera: Camera
    var zone_id: String? = null
    var path_photo: String? = null
    var situation: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_detail_location)
        viewmodel = ViewModelProvider(this).get(DetailLocationViewModel::class.java)
        binding.viewmodel = viewmodel

        toolbar.title = ""
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        ivSituation.setTag("")
        camera = Camera.Builder()
                .resetToCorrectOrientation(true)
                .setTakePhotoRequestCode(1)
                .setDirectory("SecurityPatrol")
                .setName("patrol_"+System.currentTimeMillis())
                .setImageFormat(Camera.IMAGE_JPG)
                .setCompression(75)
                .setImageHeight(500)
                .build(this)

        location = SimpleLocation(this)
        handleUIState()
        getLocationData()
        radioGroup.setOnCheckedChangeListener(this)
        etUserName.setText(SharedPrefManager.getString(this,"name"))
        btnTakePhoto.setOnClickListener(this)
    }

    private fun getLocationData() {
        val idJson: JSONObject = JSONObject(intent.getStringExtra("locID")!!)
        zone_id = idJson.getString("zone_id")
        viewmodel.locID = idJson.getString("zone_id")
        viewmodel.loadLocationData()
    }

    private fun handleUIState(){
        viewmodel.getState().observer(this, androidx.lifecycle.Observer {
            when(it){
                is ViewState.IsLoading ->{
                    containerDetailLocation.snackbar("Loading...")
                }
                is ViewState.Error-> {
                    containerDetailLocation.snackbar(it.err!!)
                }
                is ViewState.SuccessMessage -> {
                    val data: WebResponse<LocationData> = it.value as WebResponse<LocationData>
                    etName.setText(data.data!!.name)
                    etLatitude.setText(data.data!!.latitude)
                    etLongitude.setText(data.data!!.longitude)
                }
            }
        })
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = SupportMenuInflater(this)
        inflater.inflate(R.menu.menu_detail_location,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menuNext -> {
                val checkedRadioButtonID = radioGroup.checkedRadioButtonId
                if(checkedRadioButtonID == View.NO_ID){
                    containerDetailLocation.snackbar("Pilih status situasi terlebih dulu!")
                }
                else if(ivSituation.tag == ""){
                    containerDetailLocation.snackbar("Ambil foto situasi terlebih dulu!")
                }
                else {
                    val intent = Intent(this,DetectorActivity::class.java)
                    intent.putExtra("isFirst","")
                    intent.putExtra("bmpUri",SharedPrefManager.getString(this,"uriDataSet").toUri())
                    intent.putExtra("securityName",SharedPrefManager.getString(this,"name"))
                    intent.putExtra("lat",etUserLatitude.text.toString())
                    intent.putExtra("long",etUserLongitude.text.toString())
                    intent.putExtra("zoneID",zone_id)
                    intent.putExtra("status",situation)
                    intent.putExtra("note",etNote.text.toString())
                    intent.putExtra("imgSituation",path_photo)
                    startActivity(intent)
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        Dexter.withContext(this).withPermission(Manifest.permission.ACCESS_COARSE_LOCATION).withListener(this).check()
    }

    override fun onPause() {
        location.endUpdates()
        super.onPause()
    }

    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
        if(!location.hasLocationEnabled()){
            SimpleLocation.openSettings(this)
        }
        location.beginUpdates()
        etUserLatitude.setText(location.latitude.toString())
        etUserLongitude.setText(location.longitude.toString())
    }

    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {

    }

    override fun onPermissionRationaleShouldBeShown(p0: PermissionRequest?, p1: PermissionToken?) {
        p1?.continuePermissionRequest()
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btnTakePhoto -> {
                try {
                    camera.takePicture()
                }
                catch (e: Exception){
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == Camera.REQUEST_TAKE_PHOTO){
            val bmp = camera.cameraBitmap
            if(bmp != null){
                ivSituation.setTag("taked")
                ivSituation.setImageBitmap(bmp)
                val imageFIleSaved = GetProperImageRotation.saveImage(bmp,this)
                path_photo = imageFIleSaved!!.absolutePath
//                toast(imageFIleSaved!!.absolutePath)
            }
        }
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        when(checkedId){
            R.id.rbAman -> {
                situation = "aman"
            }
            R.id.rbCukupAman -> {
                situation = "cukup aman"
            }
            R.id.rbBahaya -> {
                situation = "bahaya"
            }
            R.id.rbSangatBahaya -> {
                situation = "sangat bahaya"
            }
        }
    }
}