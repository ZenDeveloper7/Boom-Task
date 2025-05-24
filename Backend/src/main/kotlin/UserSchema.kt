package com.zen.boom

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.bson.Document
import org.bson.types.ObjectId

@Serializable
data class UserSchema(val id: String, val name: String, val email: String, val password: String) {
    fun toDocument(): Document = Document.parse(Json.encodeToString(this))

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromDocument(document: Document): UserSchema = json.decodeFromString(document.toJson())
    }
}

@Serializable
data class RegisterSchema(val name: String, val email: String, val password: String) {
    fun toDocument(): Document = Document.parse(Json.encodeToString(this))

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromDocument(document: Document): RegisterSchema = json.decodeFromString(document.toJson())
    }
}

@Serializable
data class LoginSchema(val email: String, val password: String) {
    fun toDocument(): Document = Document.parse(Json.encodeToString(this))

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromDocument(document: Document): LoginSchema = json.decodeFromString(document.toJson())
    }
}

class UserService(database: MongoDatabase) {
    private var collection: MongoCollection<Document>

    init {
        database.createCollection("users")
        collection = database.getCollection("users")
    }

    suspend fun findByEmail(email: String): String? = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("email", email)).first()?.getObjectId("_id")?.toHexString()
    }

    suspend fun create(car: RegisterSchema): String = withContext(Dispatchers.IO) {
        val doc = car.toDocument()
        collection.insertOne(doc)
        doc["_id"].toString()
    }
}

