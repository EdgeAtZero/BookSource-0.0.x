package io.github.edgeatzero.booksource.dmzj.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias SearchValResponse = List<SearchValResponseItem>

@Serializable
data class SearchValResponseItem(
    @SerialName("chapter_url")
    val chapterUrl: String, // //manhua.dmzj.com/../hellohello/67845.shtml
    @SerialName("chapter_url_raw")
    val chapterUrlRaw: String, // //manhua.dmzj.com/hellohello/67845.shtml
    @SerialName("comic_author")
    val comicAuthor: String, // こずみっく
    @SerialName("comic_cover")
    val comicCover: String, // https://images.dmzj.com/webpic/17/hellohellofengmianl.jpg
    @SerialName("comic_name")
    val comicName: String, // Hello＆Hello
    @SerialName("comic_url")
    val comicUrl: String, // //manhua.dmzj.com/hellohello
    @SerialName("comic_url_raw")
    val comicUrlRaw: String, // //manhua.dmzj.com/hellohello
    @SerialName("cover")
    val cover: String, // https://images.dmzj.com/webpic/17/hellohellofengmianl.jpg
    @SerialName("id")
    val id: Int, // 21899
    @SerialName("last_update_chapter_name")
    val lastUpdateChapterName: String, // 下篇
    @SerialName("status")
    val status: String
)