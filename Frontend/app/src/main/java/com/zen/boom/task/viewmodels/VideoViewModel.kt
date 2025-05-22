package com.zen.boom.task.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.zen.boom.task.model.VideoModel
import com.zen.boom.task.network.Resource
import com.zen.boom.task.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class VideoViewModel : ViewModel() {

    private val _uploadVideoMutableStateFlow =
        MutableStateFlow<Resource<JsonObject>>(Resource.Idle())
    val uploadVideoMutableStateFlow = _uploadVideoMutableStateFlow


    fun uploadVideo(videoModel: VideoModel) {
        viewModelScope.launch {
            _uploadVideoMutableStateFlow.value = Resource.Loading()
            val request = RetrofitClient.apiService.uploadVideo(videoModel)
            if (request.isSuccessful) {
                val data = request.body()
                if (data != null) {
                    _uploadVideoMutableStateFlow.value = Resource.Success(data)
                } else {
                    _uploadVideoMutableStateFlow.value = Resource.Error("Upload failed")
                }
            } else {
                _uploadVideoMutableStateFlow.value = Resource.Error("Upload failed")
            }
        }
    }

}