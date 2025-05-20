package com.zen.boom

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.Application
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt

fun Application.configureSecurity() {
    val jwtAudience = environment.config.propertyOrNull("jwt.audience")?.getString() ?: ""
    val jwtDomain = environment.config.propertyOrNull("jwt.domain")?.getString() ?: ""
    val jwtRealm = environment.config.propertyOrNull("jwt.realm")?.getString() ?: ""
    val jwtSecret = environment.config.propertyOrNull("jwt.secret")?.getString() ?: ""

    authentication {
        jwt("auth-jwt") {
            realm = jwtRealm
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtDomain)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(jwtAudience)) JWTPrincipal(credential.payload) else null
            }
        }
    }
}
