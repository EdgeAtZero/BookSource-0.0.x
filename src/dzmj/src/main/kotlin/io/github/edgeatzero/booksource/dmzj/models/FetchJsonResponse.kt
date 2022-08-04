package io.github.edgeatzero.booksource.dmzj.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FetchJsonResponse(
    @SerialName("data")
    val `data`: Data,
    @SerialName("msg")
    val msg: String, // OK
    @SerialName("result")
    val result: Int // 1
) {

    @Serializable
    data class Data(
        @SerialName("alone")
        val alone: List<String>,
        @SerialName("info")
        val info: Info,
        @SerialName("list")
        val list: List<ListItem>,
        @SerialName("similar")
        val similar: List<Similar>
    ) {

        @Serializable
        data class Info(
            @SerialName("authors")
            val authors: String, // こずみっく
            @SerialName("copyright")
            val copyright: String, // 0
            @SerialName("cover")
            val cover: String, // http://images.dmzj.com/webpic/17/hellohellofengmianl.jpg
            @SerialName("description")
            val description: String, // 长陆奥本
            @SerialName("direction")
            val direction: String, // 1
            @SerialName("first_letter")
            val firstLetter: String, // h
            @SerialName("id")
            val id: String, // 21899
            @SerialName("islong")
            val islong: String, // 2
            @SerialName("last_update_chapter_name")
            val lastUpdateChapterName: String, // 下篇
            @SerialName("last_updatetime")
            val lastUpdatetime: Long, // 1513679859
            @SerialName("status")
            val status: String, // 连载中
            @SerialName("subtitle")
            val subtitle: String, // hello hello
            @SerialName("title")
            val title: String, // Hello＆Hello
            @SerialName("types")
            val types: String, // ゆり/舰娘
            @SerialName("zone")
            val zone: String // 日本
        )

        @Serializable
        data class ListItem(
            @SerialName("chapter_name")
            val chapterName: String, // 下篇
            @SerialName("chapter_order")
            val chapterOrder: String, // 50
            @SerialName("comic_id")
            val comicId: String, // 21899
            @SerialName("createtime")
            val createtime: String, // 1513678417
            @SerialName("filesize")
            val filesize: String, // 8501656
            @SerialName("id")
            val id: String, // 67845
            @SerialName("updatetime")
            val updatetime: Long // 1513679862
        )

        @Serializable
        data class Similar(
            @SerialName("cover")
            val cover: String, // http://images.dmzj.com/webpic/11/mygraduation.jpg
            @SerialName("id")
            val id: String, // 14412
            @SerialName("last_update_chapter_name")
            val lastUpdateChapterName: String, // 全一话
            @SerialName("title")
            val title: String // My graduation
        )

    }

}