package com.dupat.demosecuritypatrol.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dupat.demosecuritypatrol.network.repositories.SecurityDatabaseRepository
import com.dupat.demosecuritypatrol.viewmodel.DataSetSecurityViewModel

class DataSetSecurityViewModelFactory(private val repository: SecurityDatabaseRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(DataSetSecurityViewModel::class.java)){
            return DataSetSecurityViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown View Model class")
    }

}