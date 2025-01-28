package com.example.qcounter.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qcounter.dataclass.DeviceConfiguration
import com.example.qcounter.dataclass.TicketResponse
import com.example.qcounter.network.RetrofitBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

class DeviceConfigurationViewModel (context : Context) : ViewModel() {

    private val retrofitBuilder = RetrofitBuilder(context)

    private val _getDeviceConfigurationResponse = MutableLiveData<DeviceConfiguration>()
    val getDeviceConfigurationResponse: LiveData<DeviceConfiguration> = _getDeviceConfigurationResponse

    private val _errorResponse = MutableLiveData<String?>()
    val errorResponse: LiveData<String?> = _errorResponse

    fun getDeviceConfiguration(branchId: String, deviceId: String , baseUrl : String) {
        viewModelScope.launch (Dispatchers.IO){
            try {
                val response = retrofitBuilder.getDeviceConfiguration(branchId, deviceId, baseUrl)
                _getDeviceConfigurationResponse.postValue(response)
            } catch (e: HttpException){
                handleHttpException(e)
            } catch (e: Exception) {
                // Handle other exceptions
                _errorResponse.postValue("Unexpected error occurred: ${e.message}")
            }
        }
    }

    private fun handleHttpException(e: HttpException) {
        when (e.code()) {
            400 -> _errorResponse.postValue("Bad Request: ${e.message}")
            401 -> _errorResponse.postValue("Unauthorized: ${e.message}")
            403 -> _errorResponse.postValue("Forbidden: ${e.message}")
            404 -> _errorResponse.postValue("Not Found: ${e.message}")
            500 -> _errorResponse.postValue("Internal Server Error: ${e.message}")
            503 -> _errorResponse.postValue("Service Unavailable: ${e.message}")
            else -> _errorResponse.postValue("HTTP error: ${e.code()} - ${e.message}")
        }
    }
}