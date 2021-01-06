package com.dupat.demosecuritypatrol.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.dupat.demosecuritypatrol.R
import com.google.android.material.snackbar.Snackbar

fun Context.toast(msg: String)
{
    Toast.makeText(this,msg, Toast.LENGTH_LONG).show()
}

fun View.snackbar(msg: String)
{
    Snackbar.make(this,msg, Snackbar.LENGTH_LONG).also { snackbar ->
        snackbar.setActionTextColor(resources.getColor(R.color.purple_700))
        snackbar.setAction("OK"){
            snackbar.dismiss()
        }
    }.show()
}

fun Context.hideKeyboard(view: View) {

    var imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken,0)

}

fun Context.showKeyboard() {
    (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.apply {
        toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }
}
