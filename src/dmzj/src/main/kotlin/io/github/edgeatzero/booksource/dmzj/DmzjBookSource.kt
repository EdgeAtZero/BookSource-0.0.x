package io.github.edgeatzero.booksource.dmzj

import io.github.edgeatzero.booksource.dmzj.models.*
import io.github.edgeatzero.booksource.dmzj.utils.RSA
import io.github.edgeatzero.booksource.dmzj.utils.parseContents
import io.github.edgeatzero.booksource.dmzj.utils.parseFetchBook
import io.github.edgeatzero.booksource.dmzj.utils.parseFetchChapter
import io.github.edgeatzero.booksource.exceptions.ParsedException
import io.github.edgeatzero.booksource.exceptions.UnsupportedMethodIndexException
import io.github.edgeatzero.booksource.extends.MultipleBookSource
import io.github.edgeatzero.booksource.functions.LocalizationFunction
import io.github.edgeatzero.booksource.functions.SearchFunction
import io.github.edgeatzero.booksource.models.*
import io.github.edgeatzero.booksource.nesteds.LogNested
import io.github.edgeatzero.booksource.preferences.Preference
import io.github.edgeatzero.booksource.preferences.PreferenceAction
import io.github.edgeatzero.booksource.preferences.SelectPreference
import io.github.edgeatzero.booksource.utils.LogHelper
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.logging.Logger as LoggerPlugin
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.protobuf.ProtoBuf
import java.nio.charset.Charset
import java.util.*

@OptIn(ExperimentalSerializationApi::class)
class DmzjBookSource : MultipleBookSource(), SearchFunction, LocalizationFunction, LogNested {
    internal companion object {
        const val KEY_KEYWORDS = "keywords"
        const val KEY_INDEX = "index"
        const val KEY_LAST_INDEX = "last_index"
        const val KEY_CLASSIFY = "classify"
        const val KEY_STATUS = "status"
        const val KEY_REGION = "region"
        const val KEY_SORT = "sort"
        const val KEY_READER = "reader"

        const val BASE_HOST = "m.dmzj.com"
        const val API_HOST = "api.dmzj.com"
        const val API_V4_HOST = "nnv4api.muwai.com"
        const val V3_API_HOST = "v3api.dmzj.com"
        const val SEARCH_HOST = "s.acg.dmzj.com"

        const val CHAR_BAR = "|"
    }

    override val maxFetchMethodIndex = 1
    override val maxContentsMethodIndex = 1
    override val maxChaptersMethodIndex = 0

    override val id = "dmzj"
    override val lang: Locale = Locale.SIMPLIFIED_CHINESE
    override val supportedLang = arrayOf(Locale.SIMPLIFIED_CHINESE)
    override val version = 11
    override val versionName = "beta 0.0.11"


    override fun install(engine: HttpClientEngine) {
        super.install(engine)
        client.plugin(HttpSend)
    }

    private lateinit var language: Properties
    private lateinit var classifyData: Map<String, String>
    private lateinit var regionData: Map<String, String>
    private lateinit var readerData: Map<String, String>
    private lateinit var statusData: Map<String, String>
    private lateinit var sortData: Map<String, String>

    override fun install(lang: Locale) {
        val properties = Properties()
        DmzjBookSource::class.java.classLoader.getResourceAsStream("$lang.properties").use {
            properties.load(requireNotNull(it).bufferedReader(Charset.forName("UTF-8")))
        }
        classifyData = JSON.decodeFromString(properties.getProperty("array_classify"))
        statusData = JSON.decodeFromString(properties.getProperty("array_status"))
        regionData = JSON.decodeFromString(properties.getProperty("array_region"))
        sortData = JSON.decodeFromString(properties.getProperty("array_sort"))
        readerData = JSON.decodeFromString(properties.getProperty("array_reader"))
        language = properties
    }

    private lateinit var logHelper: LogHelper

    override fun install(helper: LogHelper) {
        logHelper = helper
    }


    override fun HttpClientConfig<*>.clientConfig() {
        defaultRequest {
            headers {
                append("Referer", "https://www.dmzj.com/")
                userAgent("Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.93 Mobile Safari/537.36 EdgeReader/1.0")
            }
        }
        install(Logging) {
            logger = object : LoggerPlugin {
                override fun log(message: String) = this@DmzjBookSource.logHelper.log { message }
            }
            level = LogLevel.ALL
        }
    }

    override fun HttpRequestBuilder.processImageURL(url: String) = Unit


    override suspend fun fetch(id: String, method: Int): Book = when (method) {
        0 -> fetchProto(id).let(::parseFetchBook)
        1 -> fetchJson(id).let(::parseFetchBook)
        else -> throw UnsupportedMethodIndexException(method)
    }

    private suspend fun fetchProto(id: String): FetchProtoResponse = client
        .get {
            url(scheme = URLProtocol.HTTPS.name, host = API_V4_HOST) {
                path("comic", "detail", id)
                parameter("channel", "android")
                parameter("version", ".0.0")
                parameter("timestamp", System.currentTimeMillis() / 1000)
            }
        }
        .bodyAsText()
        .also { if (it.length < 200 || it.isBlank()) throw ParsedException(message = "maybe no such book for $id, please check") }
        .let(RSA::decryptProtobufData)
        .let { ProtoBuf.decodeFromByteArray(it) }

    private suspend fun fetchJson(id: String): FetchJsonResponse = client
        .get {
            url(scheme = URLProtocol.HTTPS.name, host = API_HOST) {
                path("dynamic", "comicinfo", "$id.json")
            }
        }
        .bodyAsText()
        .also { if (it.isBlank()) throw ParsedException(message = "maybe no such book for $id, please check") }
        .let { JSON.decodeFromString(it) }

    override suspend fun chapters(book: Book, method: Int): List<Chapter> = when (method) {
        0 -> fetchProto(book.id).let(::parseFetchChapter)
        1 -> fetchJson(book.id).let(::parseFetchChapter)
        else -> throw UnsupportedMethodIndexException(method)
    }

    override suspend fun contents(book: Book, chapter: Chapter, method: Int): Contents = when (method) {
        /* webpage api */
        0 -> client
            .get {
                url(scheme = URLProtocol.HTTP.name, host = BASE_HOST) {
                    path("chapinfo", book.id, "${chapter.id}.html")
                }
            }.bodyAsText()
            .let { JSON.decodeFromString<ContentsResponse>(it) }
            .let(::parseContents)

        else -> throw UnsupportedMethodIndexException(method)
    }

    private fun selectPreference(label: String, key: String, mapper: Map<String, String>, default: String? = null) =
        SelectPreference(
            id = key,
            label = label,
            selections = mapper.keys.toList(),
            action = PreferenceAction(
                saver = { input, present ->
                    val selected = present.selected ?: mapper.keys.first { mapper[it] == default }
                    input[key] = mapper.getValue(selected)
                },
                restorer = { input, previous ->
                    val selected = input[key] ?: default
                    previous.copy(selected = mapper.keys.first { mapper[it] == selected })
                }
            )
        )

    override val searchCreator by lazy { SearchCreator() }
    override val searchPreferences
        get() = listOf<Preference>(
            selectPreference(language.getProperty("label_classify"), KEY_CLASSIFY, classifyData, default = ""),
            selectPreference(language.getProperty("label_status"), KEY_STATUS, statusData, default = ""),
            selectPreference(language.getProperty("label_region"), KEY_REGION, regionData, default = ""),
            selectPreference(language.getProperty("label_sort"), KEY_SORT, sortData, default = "0"),
            selectPreference(language.getProperty("label_reader"), KEY_READER, readerData, default = "")
        )

    override suspend fun search(config: Map<String, String>): Pair<List<Book>, Map<String, String>?> =
        if (config.contains(KEY_KEYWORDS)) client
            .get {
                url(scheme = URLProtocol.HTTP.name, host = SEARCH_HOST) {
                    path("comicsum", "search.php")
                    parameter("s", config[KEY_KEYWORDS])
                }
            }
            .bodyAsText()
            .let(::searchParse) to null
        else client
            .get {
                url(scheme = URLProtocol.HTTPS.name, host = V3_API_HOST) {
                    path(
                        KEY_CLASSIFY,
                        listOf(
                            config[KEY_KEYWORDS],
                            config[KEY_CLASSIFY],
                            config[KEY_STATUS],
                            config[KEY_REGION],
                            config[KEY_READER]
                        )
                            .filter { !it.isNullOrBlank() }
                            .joinToString(separator = "-")
                            .ifBlank { "0" },
                        config[KEY_SORT] ?: "0",
                        "${config[KEY_INDEX] ?: "0"}.json"
                    )
                }
            }
            .bodyAsText()
            .let(::searchParse) to null

    private fun searchParse(response: String): List<Book> =
        if (response.contains("g_search_data"))
            JSON.decodeFromString<SearchValResponse>(response.substringAfter('=').trim().removeSuffix(";"))
                .map(::parseFetchBook)
        else
            JSON.decodeFromString<SearchJsonResponse>(response)
                .map(::parseFetchBook)

    inner class SearchCreator internal constructor() : SearchFunction.Configurer {

        override fun config(
            output: MutableMap<String, String>,
            keywords: String?,
            tags: List<TagSearched>?,
            sort: SearchSort?,
            author: String?,
            uploader: String?
        ) {
            require(tags == null && author == null && uploader == null)
            val isNotIncludeCustom = output.isEmpty()
                    || output[KEY_CLASSIFY].isNullOrBlank()
                    && output[KEY_STATUS].isNullOrBlank()
                    && output[KEY_REGION].isNullOrBlank()
                    && output[KEY_SORT].isNullOrBlank()
                    && output[KEY_READER].isNullOrBlank()
            when {
                isNotIncludeCustom && keywords != null && sort == null -> output[KEY_KEYWORDS] = keywords
                !isNotIncludeCustom && keywords == null && sort != null -> output[KEY_SORT] = when (sort) {
                    SSearchSort.Hottest -> "0"
                    SSearchSort.Newest -> "1"
                    else -> throw IllegalArgumentException("unsupported for $sort")
                }

                else -> throw IllegalArgumentException("when input keywords, the others will be disable")
            }
        }

        override fun setIndex(configs: MutableMap<String, String>, index: Int) {
            configs[KEY_INDEX] = index.toString()
        }

        override fun getIndex(configs: Map<String, String>): Int = configs[KEY_INDEX]?.toInt() ?: 0

        override fun getLastIndex(configs: Map<String, String>): Int = configs[KEY_LAST_INDEX]?.toInt() ?: 0

        override val isKeywordsSupported = true
        override val isTagsSupported = false
        override val isSearchOrderSupported = false
        override val isAuthorSupported = false
        override val isUploaderSupported = false
        override val isPageIndexSupported = true
        override val supportedSearchSorts = listOf(SSearchSort.Hottest, SSearchSort.Newest)

    }

}