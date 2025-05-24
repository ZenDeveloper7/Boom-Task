package com.zen.boom.task.viewmodels

import android.provider.MediaStore.Video
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.zen.boom.task.model.VideoModel
import com.zen.boom.task.network.Resource
import com.zen.boom.task.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.Callback
import retrofit2.Call
import retrofit2.Response

class VideoViewModel : ViewModel() {

    private val _uploadVideoMutableStateFlow =
        MutableStateFlow<Resource<JsonObject>>(Resource.Idle())
    val uploadVideoMutableStateFlow = _uploadVideoMutableStateFlow

    private val _feedVideoMutableStateFlow =
        MutableStateFlow<Resource<List<VideoModel>>>(Resource.Idle())
    val feedVideoMutableStateFlow = _feedVideoMutableStateFlow

    private val _likeVideoMutableStateFlow =
        MutableStateFlow<Resource<JsonObject>>(Resource.Idle())
    val likeVideoMutableStateFlow = _likeVideoMutableStateFlow

    private val _viewVideoMutableStateFlow =
        MutableStateFlow<Resource<JsonObject>>(Resource.Idle())
    val viewVideoMutableStateFlow = _viewVideoMutableStateFlow

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

    fun getFeed(page: Int = 1) {
        _feedVideoMutableStateFlow.value = Resource.Loading()
        RetrofitClient.apiService.getVideos(page).enqueue(object : retrofit2.Callback<List<VideoModel>> {
            override fun onResponse(
                call: Call<List<VideoModel>>,
                response: Response<List<VideoModel>>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        _feedVideoMutableStateFlow.value = Resource.Success(data)
                    } else {
                        _feedVideoMutableStateFlow.value = Resource.Error("Failed to load feed")
                    }
                } else {
                    _feedVideoMutableStateFlow.value = Resource.Error("Failed to load feed")
                }
            }

            override fun onFailure(call: Call<List<VideoModel>>, t: Throwable) {
            }

        })
    }

    fun likeVideo(videoId: String) {
        viewModelScope.launch {
            _likeVideoMutableStateFlow.value = Resource.Loading()
            val request = RetrofitClient.apiService.like(videoId)
            if (request.isSuccessful) {
                val data = request.body()
                if (data != null) {
                    _likeVideoMutableStateFlow.value = Resource.Success(data)
                } else {
                    _likeVideoMutableStateFlow.value = Resource.Error("Like failed")
                }
            } else {
                _likeVideoMutableStateFlow.value = Resource.Error("Like failed")
            }
        }
    }

    fun viewVideo(videoId: String) {
        viewModelScope.launch {
            _viewVideoMutableStateFlow.value = Resource.Loading()
            val request = RetrofitClient.apiService.view(videoId)
            if (request.isSuccessful) {
                val data = request.body()
                if (data != null) {
                    _viewVideoMutableStateFlow.value = Resource.Success(data)
                } else {
                    _viewVideoMutableStateFlow.value = Resource.Error("View failed")
                }
            } else {
                _viewVideoMutableStateFlow.value = Resource.Error("View failed")
            }
        }
    }

}