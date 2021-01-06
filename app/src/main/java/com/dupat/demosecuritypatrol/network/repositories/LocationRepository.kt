package com.dupat.demosecuritypatrol.network.repositories

import com.dupat.demosecuritypatrol.network.APIInterface
import com.dupat.demosecuritypatrol.network.SafeAPIRequest
import com.dupat.demosecuritypatrol.network.response.WebResponse
import com.dupat.demosecuritypatrol.network.response.data.LocationData
import com.dupat.demosecuritypatrol.network.response.data.LoginData

class LocationRepository : SafeAPIRequest() {
    suspend fun locationDetail(id: String) : WebResponse<LocationData> {
        return apiRequest { APIInterface().locationDetail(id) }
    }
}