package com.dupat.demosecuritypatrol.network.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dupat.demosecuritypatrol.db.dao.DataSetSecurityDao
import com.dupat.demosecuritypatrol.db.entity.DataSetSecurity

class SecurityDatabaseRepository(private val dao: DataSetSecurityDao) {

    val dataSetSecurity: LiveData<List<DataSetSecurity>> = dao.getData(1)

    suspend fun insert(dataSetSecurity: DataSetSecurity){
        dao.insertData(dataSetSecurity)
    }

    suspend fun delete(dataSetSecurity: DataSetSecurity){
        dao.deleteData(dataSetSecurity)
    }
}