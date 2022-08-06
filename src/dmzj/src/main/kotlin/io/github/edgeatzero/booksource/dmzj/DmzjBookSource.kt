package io.github.edgeatzero.booksource.dmzj

import io.github.edgeatzero.booksource.ExperimentalBookSourceApi
import io.github.edgeatzero.booksource.dmzj.models.*
import io.github.edgeatzero.booksource.dmzj.utils.RSA
import io.github.edgeatzero.booksource.dmzj.utils.parseContents
import io.github.edgeatzero.booksource.dmzj.utils.parseFetchBook
import io.github.edgeatzero.booksource.dmzj.utils.parseFetchChapter
import io.github.edgeatzero.booksource.exceptions.ParsedException
import io.github.edgeatzero.booksource.exceptions.UnsupportedMethodIndexException
import io.github.edgeatzero.booksource.extends.MultipleBookSource
import io.github.edgeatzero.booksource.functions.SearchFunction
import io.github.edgeatzero.booksource.models.*
import io.github.edgeatzero.booksource.preferences.PreferenceAction
import io.github.edgeatzero.booksource.preferences.SelectPreference
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import java.util.*

@OptIn(ExperimentalBookSourceApi::class, ExperimentalSerializationApi::class)
class DmzjBookSource : MultipleBookSource(), SearchFunction {
    internal companion object {
        private val JSON = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
        private val PROTOBUF = ProtoBuf

        private val ARRAY_CLASSIFY = mapOf(
            "全部" to "",
            "冒险" to "4",
            "百合" to "3243",
            "生活" to "3242",
            "四格" to "17",
            "伪娘" to "3244",
            "悬疑" to "3245",
            "后宫" to "3249",
            "热血" to "3248",
            "耽美" to "3246",
            "其他" to "16",
            "恐怖" to "14",
            "科幻" to "7",
            "格斗" to "6",
            "欢乐向" to "5",
            "爱情" to "8",
            "侦探" to "9",
            "校园" to "13",
            "神鬼" to "12",
            "魔法" to "11",
            "竞技" to "10",
            "历史" to "3250",
            "战争" to "3251",
            "魔幻" to "5806",
            "扶她" to "5345",
            "东方" to "5077",
            "奇幻" to "5848",
            "轻小说" to "6316",
            "仙侠" to "7900",
            "搞笑" to "7568",
            "颜艺" to "6437",
            "性转换" to "4518",
            "高清单行" to "4459",
            "治愈" to "3254",
            "宅系" to "3253",
            "萌系" to "3252",
            "励志" to "3255",
            "节操" to "6219",
            "职场" to "3328",
            "西方魔幻" to "3365",
            "音乐舞蹈" to "3326",
            "机战" to "3325"
        )
        private val ARRAY_STATUS = mapOf(
            "全部" to "",
            "连载" to "2309",
            "完结" to "2310"
        )
        private val ARRAY_REiGON = mapOf(
            "全部" to "",
            "日本" to "2304",
            "韩国" to "2305",
            "欧美" to "2306",
            "港台" to "2307",
            "内地" to "2308",
            "其他" to "8453"
        )
        private val ARRAY_SORT = mapOf(
            "人气" to "0",
            "更新" to "1"
        )
        private val ARRAY_READER = mapOf(
            "全部" to "",
            "少年" to "3262",
            "少女" to "3263",
            "青年" to "3264"
        )

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
    }

    override val maxFetchMethodIndex = 1
    override val maxContentsMethodIndex = 1
    override val maxChaptersMethodIndex = 0

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
        .let { PROTOBUF.decodeFromByteArray(it) }

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

    override val searchCreator by lazy { SearchCreator() }
    override val searchPreferences = listOf(
        SelectPreference(
            id = KEY_CLASSIFY,
            label = "分类",
            selections = ARRAY_CLASSIFY.keys.toList(),
            action = PreferenceAction(
                saver = { input, previous ->
                    previous.selected?.let { input[KEY_CLASSIFY] = it }
                    previous
                },
                restorer = { input, previous ->
                    previous.copy(selected = input[KEY_CLASSIFY])
                }
            )
        ),
        SelectPreference(
            id = KEY_STATUS,
            label = "连载状态",
            selections = ARRAY_STATUS.keys.toList(),
            action = PreferenceAction(
                saver = { input, previous ->
                    previous.selected?.let { input[KEY_STATUS] = it }
                    previous
                },
                restorer = { input, previous ->
                    previous.copy(selected = input[KEY_STATUS])
                }
            )
        ),
        SelectPreference(
            id = KEY_REGION,
            label = "地区",
            selections = ARRAY_REiGON.keys.toList(),
            action = PreferenceAction(
                saver = { input, previous ->
                    previous.selected?.let { input[KEY_REGION] = it }
                    previous
                },
                restorer = { input, previous ->
                    previous.copy(selected = input[KEY_REGION])
                }
            )
        ),
        SelectPreference(
            id = KEY_SORT,
            label = "排序",
            selections = ARRAY_SORT.keys.toList(),
            action = PreferenceAction(
                saver = { input, previous ->
                    previous.selected?.let { input[KEY_SORT] = it }
                    previous
                },
                restorer = { input, previous ->
                    previous.copy(selected = input[KEY_SORT])
                }
            )
        ),
        SelectPreference(
            id = KEY_READER,
            label = "读者",
            selections = ARRAY_READER.keys.toList(),
            action = PreferenceAction(
                saver = { input, previous ->
                    previous.selected?.let { input[KEY_READER] = it }
                    previous
                },
                restorer = { input, previous ->
                    previous.copy(selected = input[KEY_CLASSIFY])
                }
            )
        )
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
                        "classify",
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
            .let { searchParse(it) } to null

    private fun searchParse(response: String): List<Book> =
        if (response.contains("g_search_data"))
            JSON.decodeFromString<SearchValResponse>(response.substringAfter('=').trim().removeSuffix(";"))
                .map(::parseFetchBook)
        else
            JSON.decodeFromString<SearchJsonResponse>(response)
                .map(::parseFetchBook)

    inner class SearchCreator internal constructor() : SearchFunction.ConfigCreator {

        override fun buildConfig(
            keywords: String?,
            tags: List<TagSearched>?,
            sort: SearchSort?,
            author: String?,
            uploader: String?
        ): Map<String, String> {
            require(tags == null && author == null && uploader == null)
            return when {
                keywords != null && sort == null -> mapOf(KEY_KEYWORDS to keywords)
                keywords == null && sort != null -> mapOf(
                    KEY_SORT to when (sort) {
                        SSearchSort.Hottest -> "0"
                        SSearchSort.Newest -> "1"
                        else -> throw IllegalArgumentException("unsupported for $sort")
                    }
                )

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