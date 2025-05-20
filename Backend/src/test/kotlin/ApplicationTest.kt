package com.zen.boom

import io.ktor.server.testing.testApplication
import kotlin.test.Test

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        /*application {
            module()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
        }*/
    }

}
