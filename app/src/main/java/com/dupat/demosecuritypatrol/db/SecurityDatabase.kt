package com.dupat.demosecuritypatrol.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dupat.demosecuritypatrol.db.dao.DataSetSecurityDao
import com.dupat.demosecuritypatrol.db.entity.DataSetSecurity

@Database(entities = [DataSetSecurity::class],version = 1)
abstract class SecurityDatabase: RoomDatabase() {

    abstract val dataSetSecurityDao: DataSetSecurityDao

    companion object{
        @Volatile
        private var INSTANCE: SecurityDatabase? = null
        fun getInstance(context: Context): SecurityDatabase{
            synchronized(this){
                var instance = INSTANCE
                if(instance == null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        SecurityDatabase::class.java,
                        "secutity_database"
                    ).build()
                }

                return instance
            }
        }
    }

}