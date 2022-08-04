package io.github.edgeatzero.booksource.dmzj.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentsResponse(
    @SerialName("chapter_name")
    val chapterName: String, // 第07话
    @SerialName("chapter_num")
    val chapterNum: Int, // 7
    @SerialName("chapter_order")
    val chapterOrder: Int, // 80
    @SerialName("chapter_true_type")
    val chapterTrueType: Int, // 1
    @SerialName("chapter_type")
    val chapterType: Int, // 0
    @SerialName("chaptertype")
    val chaptertype: Int, // 0
    @SerialName("comic_id")
    val comicId: Int, // 45561
    @SerialName("comment_count")
    val commentCount: Int, // 8
    @SerialName("createtime")
    val createtime: Int, // 1575993311
    @SerialName("direction")
    val direction: Int, // 0
    @SerialName("download")
    val download: String,
    @SerialName("filesize")
    val filesize: Int, // 3177562
    @SerialName("folder")
    val folder: String, // y/异世界迷宫都市的治愈魔法使/第07话_1575978607
    @SerialName("hidden")
    val hidden: Int, // 2
    @SerialName("high_file_size")
    val highFileSize: Int, // 0
    @SerialName("hit")
    val hit: Int, // 0
    @SerialName("id")
    val id: Int, // 95618
    @SerialName("link")
    val link: String,
    @SerialName("message")
    val message: String,
    @SerialName("page_url")
    val pageUrl: List<String>,
    @SerialName("picnum")
    val picnum: Int, // 22
    @SerialName("prev_chap_id")
    val prevChapId: Int? = null, // 88840
    @SerialName("sns_tag")
    val snsTag: Int, // 0
    @SerialName("sum_pages")
    val sumPages: Int, // 22
    @SerialName("translator")
    val translator: String,
    @SerialName("translatorid")
    val translatorid: String,
    @SerialName("uid")
    val uid: Int, // 105816354
    @SerialName("updatetime")
    val updatetime: Int, // 1575993311
    @SerialName("username")
    val username: String // dmzj_105816354
)