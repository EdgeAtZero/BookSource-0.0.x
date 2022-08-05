package io.github.edgeatzero.booksource.dmzj.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias  SearchJsonResponse = ArrayList<SearchJsonResponseItem>

@Serializable
data class SearchJsonResponseItem(
    @SerialName("authors")
    val authors: String, // 踏雪动漫
    @SerialName("cover")
    val cover: String, // https://images.dmzj.com/img/webpic/4/1447215436.jpg
    @SerialName("id")
    val id: Int, // 20926
    @SerialName("last_updatetime")
    val lastUpdatetime: Int, // 1527297061
    @SerialName("num")
    val num: Int, // 111899250
    @SerialName("status")
    val status: String, // 连载中
    @SerialName("title")
    val title: String, // 妖神记
    @SerialName("types")
    val types: String // 冒险/热血/奇幻
)
