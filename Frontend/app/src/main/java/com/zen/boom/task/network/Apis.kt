package com.zen.boom.task.network

import com.google.gson.JsonObject
import com.zen.boom.task.model.VideoModel
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface Apis {

    @POST("/auth/login")
    suspend fun login(@Body body: JsonObject): Response<JsonObject>

    @POST("/auth/register")
    suspend fun register(@Body body: JsonObject): Response<JsonObject>

    @POST("/upload")
    suspend fun uploadVideo(@Body body: VideoModel): Response<JsonObject>

    @GET("/videos")
    fun getVideos(@Query("page") page: Int): Call<List<VideoModel>>

    @POST("/like/{id}")
    suspend fun like(@Path("id") id: String): Response<JsonObject>

    @POST("/view/{id}")
    suspend fun view(@Path("id") id: String): Response<JsonObject>
}