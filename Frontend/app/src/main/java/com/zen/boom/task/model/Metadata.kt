package com.zen.boom.task.model

data class Metadata(
    val timestamp: Long,
    val uploadedBy: String,
    val lastViewed: Long,
    val lastLiked: Long,
)