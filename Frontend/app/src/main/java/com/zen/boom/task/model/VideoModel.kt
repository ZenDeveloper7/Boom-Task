package com.zen.boom.task.model

data class VideoModel(
    val id: String?,
    val metadata: Metadata,
    val thumbnailUrl: String,
    val title: String,
    val videoUrl: String
)
