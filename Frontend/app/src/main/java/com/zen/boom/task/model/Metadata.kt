package com.zen.boom.task.model

data class Metadata(
    val uploadedAt: Long,
    val likes: List<String>,
    val views: List<String>,
    var uploadedBy: String,
)