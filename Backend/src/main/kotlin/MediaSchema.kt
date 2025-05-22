package com.zen.boom

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Indexes.descending
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.bson.Document


@Serializable
data class MediaSchema(
    val title: String,
    val videoUrl: String,
    val thumbnailUrl: String,
    val metadata: MediaMetadataSchema,
) {
    fun toDocument(): Document = Document.parse(Json.encodeToString(this))

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromDocument(document: Document): UserSchema = json.decodeFromString(document.toJson())
    }
}

@Serializable
data class MediaMetadataSchema(
    val uploadedAt: Long,
    val likes: List<String>,
    val views: List<String>,
    var uploadedBy: String,
) {
    fun toDocument(): Document = Document.parse(Json.encodeToString(this))

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromDocument(document: Document): UserSchema = json.decodeFromString(document.toJson())
    }
}

@Serializable
data class LikeAndViewSchema(
    val userId: String,
    val videoId: String,
)

class MediaService(database: MongoDatabase) {
    var collection: MongoCollection<Document>

    init {
        database.createCollection("videos")
        collection = database.getCollection("videos")
    }

    suspend fun upload(media: MediaSchema) = withContext(Dispatchers.IO) {
        val doc = media.toDocument()
        collection.insertOne(doc)
        doc["_id"].toString()
    }

    suspend fun getAllVideos(page: Int = 1, pageSize: Int = 10): List<MediaSchema> =
        withContext(Dispatchers.IO) {
            val skip = (page - 1) * pageSize

            collection.find()
                .skip(skip)
                .limit(pageSize)
                .sort(descending("uploadedAt"))
                .toList()
                .map { doc ->
                    MediaSchema(
                        title = doc.getString("title"),
                        videoUrl = doc.getString("videoUrl"),
                        thumbnailUrl = doc.getString("thumbnailUrl"),
                        metadata = MediaMetadataSchema(
                            uploadedAt = doc.getLong("uploadedAt"),
                            likes = doc.getList("likes", String::class.java),
                            views = doc.getList("views", String::class.java),
                            uploadedBy = doc.getString("uploadedBy")
                        )
                    )
                }
        }

    suspend fun like(like: LikeAndViewSchema) {
        withContext(Dispatchers.IO) {
            collection.updateOne(
                Document("_id", like.videoId),
                Document("\$addToSet", Document("likes", like.userId))
            )
        }
    }

    suspend fun view(like: LikeAndViewSchema) {
        withContext(Dispatchers.IO) {
            collection.updateOne(
                Document("_id", like.videoId),
                Document("\$addToSet", Document("views", like.userId))
            )
        }
    }
}