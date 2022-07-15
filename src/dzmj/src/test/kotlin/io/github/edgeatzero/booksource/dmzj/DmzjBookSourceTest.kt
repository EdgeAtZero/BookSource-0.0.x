package io.github.edgeatzero.booksource.dmzj

import io.ktor.client.engine.okhttp.*
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class DmzjBookSourceTest {

    private val source by lazy { DmzjBookSource() }

    @BeforeTest
    fun setUp() {
        source.install(OkHttpEngine(OkHttpConfig()))
    }

//    @Test
    fun `fetch does-not-exist book`() {
        assertThrows<Throwable> { runBlocking { source.fetch("") } }
    }

    @Test
    fun `fetch 63559 book`() {
        assertDoesNotThrow {
            runBlocking {
                source.fetch("63559")
            }
        }
    }

    @Test
    fun `search book`() {
        assertDoesNotThrow {
            runBlocking {
                val config = source.SearchCreator().buildConfig(keywords = "世界")
                val books = source.search(config).fetch()
                println(books)
            }
        }
    }

    @AfterTest
    fun tearDown() {
        source.close()
    }

}