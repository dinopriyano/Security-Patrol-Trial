package com.dupat.demosecuritypatrol.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "data_set_security")
data class DataSetSecurity (

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "imageName")
    val imageName: String,

    @ColumnInfo(name = "imageUrl")
    val imageUrl: String,

    @ColumnInfo(name = "imageData", typeAffinity = ColumnInfo.BLOB)
    val imageData: ByteArray
)