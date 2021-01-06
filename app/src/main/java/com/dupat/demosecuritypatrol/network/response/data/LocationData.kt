package com.dupat.demosecuritypatrol.network.response.data

import com.google.gson.annotations.SerializedName

class LocationData (

    var id: Int,
    var company_id: Int,
    var name: String,
    var latitude: String,
    var longitude: String,
    var qr_code: String

)