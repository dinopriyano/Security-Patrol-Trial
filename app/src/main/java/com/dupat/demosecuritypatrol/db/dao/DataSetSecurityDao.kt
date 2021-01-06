package com.dupat.demosecuritypatrol.db.dao

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import com.dupat.demosecuritypatrol.db.entity.DataSetSecurity

@Dao
interface DataSetSecurityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertData(dataSetSecurity: DataSetSecurity)

    @Delete
    suspend fun deleteData(dataSetSecurity: DataSetSecurity)

    @Query("DELETE FROM data_set_security")
    suspend fun deleteAllData()

    @Query("SELECT * FROM data_set_security WHERE id = :id")
    fun getData(id: Int) : LiveData<List<DataSetSecurity>>

    @Query("SELECT * FROM data_set_security")
    fun getAllData() : LiveData<List<DataSetSecurity>>

}