package com.zen.boom.task.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.zen.boom.task.Session
import com.zen.boom.task.network.Resource
import com.zen.boom.task.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class AuthenticationViewModel : ViewModel() {

    private val _loginMutableStateFlow = MutableStateFlow<Resource<JsonObject>>(Resource.Idle())
    val loginMutableStateFlow = _loginMutableStateFlow

    private val _registerMutableStateFlow = MutableStateFlow<Resource<JsonObject>>(Resource.Idle())
    val registerMutableStateFlow = _registerMutableStateFlow

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _registerMutableStateFlow.value = Resource.Loading()
            val body = JsonObject().apply {
                addProperty("email", email)
                addProperty("name", name)
                addProperty("password", password)
            }
            try {
                val response = RetrofitClient.apiService.register(body)
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        _registerMutableStateFlow.value = Resource.Success(data)
                    } else {
                        _registerMutableStateFlow.value = Resource.Error("Registration failed")
                    }
                } else {
                    _registerMutableStateFlow.value = Resource.Error("Registration failed")
                }
            } catch (e: Exception) {
                _registerMutableStateFlow.value = Resource.Error("Registration failed")
            }
        }
    }


    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginMutableStateFlow.value = Resource.Loading()
            val body = JsonObject().apply {
                addProperty("email", email)
                addProperty("password", password)
            }
            try {
                val request = RetrofitClient.apiService.login(body)
                if (request.isSuccessful) {
                    val data = request.body()
                    if (data != null) {
                        Session.token = data.get("token").asString
                        _loginMutableStateFlow.value = Resource.Success(data)
                    } else {
                        _loginMutableStateFlow.value = Resource.Error("Login failed")
                    }
                } else {
                    _loginMutableStateFlow.value = Resource.Error("Login failed")
                }
            } catch (e: Exception) {
                _loginMutableStateFlow.value = Resource.Error("Login failed")
            }
        }
    }
}