package com.zen.boom.task.model

data class VideoModel(
    val id: String,
    val title: String,
    val url: String,
    val likes: Long,
    val views: Long,
    val metadata: Metadata
)