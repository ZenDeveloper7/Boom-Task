package com.zen.boom.task.model

import android.util.Base64
import java.io.FileInputStream
import java.util.Properties
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object CryptoUtil {
    val SECRET_KEY: String by lazy {
        val props = Properties()
        FileInputStream("local.properties").use { props.load(it) }
        props.getProperty("secret") ?: throw IllegalStateException("Secret not found")
    }

    fun encrypt(input: String): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        val keySpec = SecretKeySpec(SECRET_KEY.toByteArray(), "AES")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec)
        val encrypted = cipher.doFinal(input.toByteArray())
        return Base64.encodeToString(encrypted, Base64.DEFAULT)
    }
}