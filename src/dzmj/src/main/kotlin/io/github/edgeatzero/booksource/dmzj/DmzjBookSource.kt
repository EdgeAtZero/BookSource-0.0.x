@file:Suppress("OPT_IN_USAGE")

package io.github.edgeatzero.booksource.dmzj

import io.github.edgeatzero.booksource.dmzj.models.*
import io.github.edgeatzero.booksource.dmzj.utils.RSA
import io.github.edgeatzero.booksource.extends.NetworkBookSource
import io.github.edgeatzero.booksource.functions.SearchFunction
import io.github.edgeatzero.booksource.models.*
import io.github.edgeatzero.booksource.preferences.HintPreference
import io.github.edgeatzero.booksource.utils.PagingController
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import java.util.*

class DmzjBookSource : NetworkBookSource(), SearchFunction {
    internal companion object {
        private val JSON = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
        private val PROTOBUF = ProtoBuf

        const val KEY_KEYWORDS = "keywords"

        const val BASE_URL = "https://m.dmzj.com"
        const val API = "https://api.dmzj.com"
        const val API_V3 = "https://v3api.dmzj.com"
        const val API_V4 = "https://nnv4api.muwai.com" // https://v4api.dmzj1.com
        const val API_PAGE_LIST_OLD = "https://api.m.dmzj.com"
        const val API_PAGE_LIST_OLD_WEBVIEW = "https://m.dmzj.com/chapinfo"
        const val CDN_IMAGE = "https://images.dmzj.com"
        const val CDN_IMAGE_SMALL = "https://imgsmall.dmzj.com"
    }

    override val id = "dmzj"
    override val lang: Locale = Locale.SIMPLIFIED_CHINESE

    override fun install() = Unit

    override fun install(engine: HttpClientEngine) {
        super.install(engine)
        client.plugin(HttpSend)
    }

    override fun HttpClientConfig<*>.clientConfig() {
        defaultRequest {
            headers {
                append("Referer", "https://www.dmzj.com/")
                userAgent("Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.93 Mobile Safari/537.36 EdgeReader/1.0")
            }
        }
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.ALL
        }
    }

    override fun HttpRequestBuilder.processImageURL(url: String) = Unit

    private suspend fun detail1(id: String): Fetch1Response = client
        .get("$API_V4/comic/detail/$id?channel=android,version=3.0.0,timestamp=${System.currentTimeMillis() / 1000}")
        .bodyAsText()
        .let(RSA::decryptProtobufData)
        .let { PROTOBUF.decodeFromByteArray(it) }

    private suspend fun detail2(id: String): Fetch2Response = client
        .get("$API/dynamic/comicinfo/$id.json")
        .bodyAsText()
        .let { JSON.decodeFromString(it) }

    override suspend fun fetch(id: String): Book = runCatching {
        detail1(id).let(::parseFetchBook)
    }.getOrElse {
        detail2(id).let(::parseFetchBook)
    }

    override suspend fun chapters(book: Book): List<Chapter> = runCatching {
        detail1(id).let(::parseFetchChapter)
    }.getOrElse {
        detail2(id).let(::parseFetchChapter)
    }

    override suspend fun contents(chapter: Chapter): Contents {
        TODO("Not yet implemented")
    }

    override val searchCreator by lazy { SearchCreator() }
    override val searchPreferences = listOf(HintPreference("Test"))

    override suspend fun search(configs: Map<String, Any>): PagingController<List<Book>> =
        object : PagingController<List<Book>>() {
            private val page = 0
            override suspend fun fetch(): List<Book> {
                return client.get {
                    url(scheme = "http", host = "s.acg.dmzj.com", path = "/comicsum/search.php") {
                        parameter("s", configs[KEY_KEYWORDS] as String)
                    }
                }.bodyAsText().let { searchParse(it) }
            }

            override fun nextPage() {
                TODO("Not yet implemented")
            }

            override fun previousPage() {
                TODO("Not yet implemented")
            }

        }

    private fun searchParse(response: String): List<Book> {
        return if (response.contains("g_search_data")) {
            simpleSearchJsonParse(response.substringAfter('=').trim().removeSuffix(";"))
        } else {
            TODO()
        }
    }

    private fun simpleSearchJsonParse(response: String): List<Book> {
        return JSON.decodeFromString<SearchResponse>("""{"items":$response}""")
            .items
            .map(::parseFetchBook)
    }

    inner class SearchCreator internal constructor() : SearchFunction.ConfigCreator {

        override fun buildConfig(
            keywords: String?,
            tags: List<TagSearched>?,
            order: SearchOrder?,
            author: String?,
            uploader: String?
        ): Map<String, Any> {
            requireNotNull(keywords) { "keywords should not be null" }
            return mapOf(KEY_KEYWORDS to keywords)
        }

        override val isKeywordsSupported = true
        override val isTagsSupported = true
        override val isSearchOrderSupported = true
        override val supportedSearchOrders = listOf(SSearchOrder.Hottest)
        override val isAuthorSupported = true
        override val isUploaderSupported = false

    }

}