package com.zen.boom.task.network

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface Apis {

    @POST("/auth/login")
    fun login(@Body body: JsonObject): Call<JsonObject>

    @POST("/auth/register")
    fun register(@Body body: JsonObject): Call<JsonObject>

    @POST("/upload")
    fun uploadVideo(name: String, email: String, password: String): Call<JsonObject>

    @GET("/videos")
    fun getVideos(): Call<JsonObject>

    @POST("/like/{id}")
    fun like(@Path("id") id: String): Call<JsonObject>

    @POST("/view/{id}")
    fun view(@Path("id") id: String): Call<JsonObject>
}