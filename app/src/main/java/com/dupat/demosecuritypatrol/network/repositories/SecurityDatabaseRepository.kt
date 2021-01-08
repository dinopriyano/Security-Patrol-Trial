package com.dupat.demosecuritypatrol.network.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dupat.demosecuritypatrol.db.dao.DataSetSecurityDao
import com.dupat.demosecuritypatrol.db.entity.DataSetSecurity
import com.dupat.demosecuritypatrol.network.APIInterface
import com.dupat.demosecuritypatrol.network.SafeAPIRequest
import com.dupat.demosecuritypatrol.network.response.WebResponse
import com.dupat.demosecuritypatrol.network.response.data.LoginData
import com.dupat.demosecuritypatrol.network.response.data.ReportData
import okhttp3.MultipartBody
import okhttp3.RequestBody

class SecurityDatabaseRepository(private val dao: DataSetSecurityDao):SafeAPIRequest() {

    val dataSetSecurity: LiveData<List<DataSetSecurity>> = dao.getData(1)

    suspend fun insert(dataSetSecurity: DataSetSecurity){
        dao.insertData(dataSetSecurity)
    }

    suspend fun delete(dataSetSecurity: DataSetSecurity){
        dao.deleteData(dataSetSecurity)
    }

    suspend fun addReport(photo: MultipartBody.Part,user_id: RequestBody,zone_id: RequestBody, status: RequestBody, latitude: RequestBody, longitude: RequestBody, note: RequestBody) : WebResponse<ReportData> {
        return apiRequest { APIInterface().addReport(photo,user_id,zone_id, status, latitude, longitude, note) }
    }
}