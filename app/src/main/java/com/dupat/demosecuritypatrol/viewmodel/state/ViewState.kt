package com.dupat.demosecuritypatrol.viewmodel.state

sealed class ViewState {
    data class OnRun(var what : Int? = null ) : ViewState()
    data class ShowToast(var message : String) : ViewState()
    data class IsLoading(var state : Boolean = false) : ViewState()
    data class Error(var err : String? = null,var viewErr: String? = null) : ViewState()
    data class IsSuccess(var what : Int? = null) : ViewState()
    data class SuccessMessage(var value: Any): ViewState()
    object Reset : ViewState()
}