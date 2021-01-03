package com.dupat.demosecuritypatrol.fragment

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.dupat.demosecuritypatrol.DetailLocationActivity
import com.dupat.demosecuritypatrol.R
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener

class ReportFragment : Fragment(),PermissionListener {

    private lateinit var codeScanner: CodeScanner

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_report, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val scannerView = view.findViewById<CodeScannerView>(R.id.scanner_view)
        val activity = requireActivity()
        codeScanner = CodeScanner(activity, scannerView)
        codeScanner.decodeCallback = DecodeCallback {
            activity.runOnUiThread {
                Toast.makeText(activity, it.text, Toast.LENGTH_LONG).show()
                val intent = Intent(activity,DetailLocationActivity::class.java)
                intent.putExtra("locID",it.text)
                startActivity(intent)
                activity.overridePendingTransition(R.anim.slide_in_right,R.anim.stay)
            }
        }
        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    override fun onResume() {
        super.onResume()
        requestCameraPermission()
    }

    private fun requestCameraPermission() {
        Dexter.withActivity(activity).withPermission(Manifest.permission.CAMERA).withListener(this).check()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        when(hidden){
            true -> {
                codeScanner.releaseResources()
            }
            false -> {
                requestCameraPermission()
            }
        }
    }

    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
        codeScanner.startPreview()
    }

    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
        requestCameraPermission()
    }

    override fun onPermissionRationaleShouldBeShown(p0: PermissionRequest?, p1: PermissionToken?) {
        p1!!.continuePermissionRequest()
    }
}