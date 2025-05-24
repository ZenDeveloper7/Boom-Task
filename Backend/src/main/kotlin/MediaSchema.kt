package com.zen.boom

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Indexes.descending
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.bson.Document
import org.bson.types.ObjectId


@Serializable
data class MediaSchema(
    val id: String? = null,
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

        fun fromDocument(document: Document): MediaMetadataSchema =
            json.decodeFromString(document.toJson())

        fun fromJson(jsonString: String): MediaMetadataSchema {
            return json.decodeFromString(jsonString)
        }
    }
}

@Serializable
data class LikeAndViewSchema(
    val videoId: String,
) {
    fun toDocument(): Document = Document.parse(Json.encodeToString(this))

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromDocument(document: Document): LikeAndViewSchema =
            json.decodeFromString(document.toJson())
    }
}

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
                        id = doc.getObjectId("_id").toHexString(),
                        title = doc.getString("title"),
                        videoUrl = doc.getString("videoUrl"),
                        thumbnailUrl = doc.getString("thumbnailUrl"),
                        metadata = MediaMetadataSchema.fromDocument(doc["metadata"] as Document)
                    )
                }
        }

    suspend fun like(videoId: String, userId: String) {
        withContext(Dispatchers.IO) {
            collection.updateOne(
                Document("_id", ObjectId(videoId)),
                Document("\$addToSet", Document("metadata.likes", userId))
            )
        }
    }

    suspend fun view(videoId: String, userId: String) {
        withContext(Dispatchers.IO) {
            collection.updateOne(
                Document("_id", ObjectId(videoId)),
                Document("\$addToSet", Document("metadata.views", userId))
            )
        }
    }
}