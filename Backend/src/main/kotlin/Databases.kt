package com.zen.boom

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.google.gson.Gson
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.ServerApi
import com.mongodb.ServerApiVersion
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import java.util.Date

fun Application.configureDatabases() {
    val mongoDatabase = connectToMongoDB()
    val userService = UserService(mongoDatabase)
    val mediaService = MediaService(mongoDatabase)

    val jwtAudience = environment.config.propertyOrNull("jwt.audience")?.getString() ?: ""
    val jwtDomain = environment.config.propertyOrNull("jwt.domain")?.getString() ?: ""
    val jwtSecret = environment.config.propertyOrNull("jwt.secret")?.getString() ?: ""

    routing {
        get("/") {
            call.respond("Working!!")
        }
        //Register
        post("/auth/register") {
            val registerRequest = call.receive<UserSchema>()
            val existingUser = userService.findByEmail(registerRequest.email)
            if (existingUser != null) {
                call.respond(HttpStatusCode.Conflict, "User already exists")
                return@post
            }
            userService.create(registerRequest)
            call.respond(HttpStatusCode.Created, "User registered successfully")
        }
        //Login
        post("/auth/login") {
            val loginRequest = call.receive<LoginSchema>()
            val user = userService.findByEmail(loginRequest.email)
            if (user == null) {
                call.respond(HttpStatusCode.Unauthorized, "User Not Found")
                return@post
            }
            if (loginRequest.password == user.password) {
                val token = JWT.create()
                    .withAudience(jwtAudience)
                    .withIssuer(jwtDomain)
                    .withClaim("email", user.email)
                    .withExpiresAt(Date(System.currentTimeMillis() + 60_000 * 60)) // 1 hour
                    .sign(Algorithm.HMAC256(jwtSecret))
                call.respond(mapOf("token" to token))
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
            }
        }
        //Get all videos
        authenticate("auth-jwt") {
            get("/videos") {
                val principal = call.principal<JWTPrincipal>()
                principal?.let {
                    val expiresAt = principal.payload.expiresAt
                    val isExpired = expiresAt.before(Date())

                    if (isExpired) {
                        call.respond(HttpStatusCode.Unauthorized, "Token expired")
                        return@get
                    }

                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                    val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 10

                    val videos = mediaService.getAllVideos(page, pageSize)
                    call.respond(HttpStatusCode.OK, videos)
                } ?: run {
                    call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
                }
            }
        }

        authenticate("auth-jwt") {
            post("/upload") {
                val principal = call.principal<JWTPrincipal>()
                principal?.let {
                    val expiresAt = principal.payload.expiresAt
                    val isExpired = expiresAt.before(Date())
                    if (!isExpired) {
                        val userEmail = principal.payload.getClaim("email").toString()
                        val metadata = call.receive<MediaSchema>()
                        if (userEmail == metadata.metadata.uploadedBy) {
                            mediaService.upload(metadata)
                            call.respond(HttpStatusCode.Created, "Uploaded successfully")
                        } else {
                            call.respond(HttpStatusCode.Unauthorized, "Uploaded by wrong person")
                        }
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, "Token expired")
                    }
                } ?: run {
                    call.respond(HttpStatusCode.Unauthorized, "Principal Not Found")
                }
            }
        }

        authenticate("auth-jwt") {
            post("/like") {
                val principal = call.principal<JWTPrincipal>()
                principal?.let {
                    val expiresAt = principal.payload.expiresAt
                    val isExpired = expiresAt.before(Date())
                    if (!isExpired) {
                        val userEmail = principal.payload.getClaim("email").toString()
                        val like = call.receive<LikeAndViewSchema>()
                        if (userEmail == like.userId) {
                            mediaService.like(like)
                            call.respond(HttpStatusCode.Created, "Liked successfully")
                        } else {
                            call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
                        }
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, "Token expired")
                    }
                } ?: run {
                    call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
                }
            }
        }

        authenticate("auth-jwt") {
            post("/view") {
                val principal = call.principal<JWTPrincipal>()
                principal?.let {
                    val expiresAt = principal.payload.expiresAt
                    val isExpired = expiresAt.before(Date())
                    if (!isExpired) {
                        val userEmail = principal.payload.getClaim("email").toString()
                        val view = call.receive<LikeAndViewSchema>()
                        if (userEmail == view.userId) {
                            mediaService.view(view)
                            call.respond(HttpStatusCode.Created, "Viewed successfully")
                        } else {
                            call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
                        }
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, "Token expired")
                    }
                } ?: run {
                    call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
                }
            }
        }
    }
}

fun Application.connectToMongoDB(): MongoDatabase {
    val connectionString =
        environment.config.propertyOrNull("mongoDb.connection")?.getString() ?: ""
    val database =
        environment.config.propertyOrNull("mongoDb.database")?.getString() ?: ""

    val serverApi = ServerApi.builder()
        .version(ServerApiVersion.V1)
        .build()

    val mongoClientSettings = MongoClientSettings.builder()
        .applyConnectionString(ConnectionString(connectionString))
        .serverApi(serverApi)
        .build()

    val mongoClient = MongoClients.create(mongoClientSettings)

    monitor.subscribe(ApplicationStopped) {
        mongoClient.close()
    }

    return mongoClient.getDatabase(database)
}
