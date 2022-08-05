package io.github.edgeatzero.booksource.dmzj.utils

import io.ktor.util.*
import java.io.ByteArrayOutputStream
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.Cipher
import kotlin.math.min

object RSA {
    private val KEY by lazy {
        "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAK8nNR1lTnIfIes6oRWJNj3mB6OssDGx0uGMpgpbVCpf6+VwnuI2stmhZNoQcM417Iz7WqlPzbUmu9R4dEKmLGEEqOhOdVaeh9Xk2IPPjqIu5TbkLZRxkY3dJM1htbz57d/roesJLkZXqssfG5EJauNc+RcABTfLb4IiFjSMlTsnAgMBAAECgYEAiz/pi2hKOJKlvcTL4jpHJGjn8+lL3wZX+LeAHkXDoTjHa47g0knYYQteCbv+YwMeAGupBWiLy5RyyhXFoGNKbbnvftMYK56hH+iqxjtDLnjSDKWnhcB7089sNKaEM9Ilil6uxWMrMMBH9v2PLdYsqMBHqPutKu/SigeGPeiB7VECQQDizVlNv67go99QAIv2n/ga4e0wLizVuaNBXE88AdOnaZ0LOTeniVEqvPtgUk63zbjl0P/pzQzyjitwe6HoCAIpAkEAxbOtnCm1uKEp5HsNaXEJTwE7WQf7PrLD4+BpGtNKkgja6f6F4ld4QZ2TQ6qvsCizSGJrjOpNdjVGJ7bgYMcczwJBALvJWPLmDi7ToFfGTB0EsNHZVKE66kZ/8Stx+ezueke4S556XplqOflQBjbnj2PigwBN/0afT+QZUOBOjWzoDJkCQClzo+oDQMvGVs9GEajS/32mJ3hiWQZrWvEzgzYRqSf3XVcEe7PaXSd8z3y3lACeeACsShqQoc8wGlaHXIJOHTcCQQCZw5127ZGs8ZDTSrogrH73Kw/HvX55wGAeirKYcv28eauveCG7iyFR0PFB/P/EDZnyb+ifvyEFlucPUI0+Y87F"
            .decodeBase64Bytes()
            .let { PKCS8EncodedKeySpec(it) }
            .let { KeyFactory.getInstance("RSA").generatePrivate(it) }
    }

    @JvmStatic
    fun decryptProtobufData(response: String): ByteArray {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, KEY)
        val stream = ByteArrayOutputStream()
        response.decodeBase64Bytes().inputStream().use { `in` ->
            stream.use { out ->
                val buffer = ByteArray(128)
                var read: Int
                while (`in`.available() > 0) {
                    read = `in`.read(buffer, 0, min(buffer.size, `in`.available()))
                    val output = cipher.doFinal(buffer, 0, read)
                    out.write(output)
                }
            }
        }
        return stream.toByteArray()
    }
}