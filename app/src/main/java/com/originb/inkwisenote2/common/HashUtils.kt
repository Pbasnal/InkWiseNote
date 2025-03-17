package com.originb.inkwisenote2.common

import java.security.MessageDigest

object HashUtils {
    fun calculateSha256(data: ByteArray?): String? {
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(data)

            // Convert hash into hexadecimal string
            val hexString = StringBuilder()
            for (b in hashBytes) {
                val hex = Integer.toHexString(0xFF and b.toInt())
                if (hex.length == 1) hexString.append('0')
                hexString.append(hex)
            }

            return hexString.toString()
        } catch (ex: Exception) {
            ex.printStackTrace()
            return null
        }
    }
}
