package com.dupat.demosecuritypatrol.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.dupat.demosecuritypatrol.network.repositories.LocationRepository
import com.dupat.demosecuritypatrol.network.repositories.LoginRepository
import com.dupat.demosecuritypatrol.utils.APIExceptions
import com.dupat.demosecuritypatrol.utils.Corountines
import com.dupat.demosecuritypatrol.utils.SingleLiveEvent
import com.dupat.demosecuritypatrol.viewmodel.state.ViewState

class DetailLocationViewModel: ViewModel() {

    var locID: String? = null
    private var state : SingleLiveEvent<ViewState> = SingleLiveEvent()

    fun loadLocationData(){
        state.value = ViewState.IsLoading(true)
        Corountines.main {
            try {
                val response = LocationRepository().locationDetail(locID!!)
                response.let {
                    state.value = ViewState.IsSuccess(1)
                    state.value = ViewState.SuccessMessage(it)
                    Log.d("DataBoss", it.data?.name!!)
                    return@main
                }

                state.value = ViewState.Error(response.message!!)
            }
            catch (e: APIExceptions)
            {
                state.value = ViewState.Error(e.message!!)
            }
        }
    }

    fun getState() = state
}