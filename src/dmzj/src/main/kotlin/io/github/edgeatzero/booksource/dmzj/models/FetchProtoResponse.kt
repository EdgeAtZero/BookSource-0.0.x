@file:Suppress("OPT_IN_USAGE")

package io.github.edgeatzero.booksource.dmzj.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
data class FetchProtoResponse(
    @ProtoNumber(1)
    val Errno: Int = 0,
    @ProtoNumber(2)
    val Errmsg: String = "",
    @ProtoNumber(3)
    val Data: DataInfo,
) {

    @Serializable
    data class DataInfo(
        @ProtoNumber(1)
        val Id: Int,
        @ProtoNumber(2)
        val Title: String,
        @ProtoNumber(3)
        val Direction: Int? = null,
        @ProtoNumber(4)
        val Islong: Int? = null,
        @ProtoNumber(5)
        val IsDmzj: Int? = null,
        @ProtoNumber(6)
        val Cover: String,
        @ProtoNumber(7)
        val Description: String,
        @ProtoNumber(8)
        val LastUpdatetime: Long,
        @ProtoNumber(9)
        val LastUpdateChapterName: String? = null,
        @ProtoNumber(10)
        val Copyright: Int? = null,
        @ProtoNumber(11)
        val FirstLetter: String? = null,
        @ProtoNumber(12)
        val ComicPy: String? = null,
        @ProtoNumber(13)
        val Hidden: Int? = null,
        @ProtoNumber(14)
        val HotNum: Int? = null,
        @ProtoNumber(15)
        val HitNum: Int? = null,
        @ProtoNumber(16)
        val Uid: Int? = null,
        @ProtoNumber(17)
        val IsLock: Int? = null,
        @ProtoNumber(18)
        val LastUpdateChapterId: Int? = null,
        @ProtoNumber(19)
        val TypesTypes: List<TypeItem> = emptyList(),
        @ProtoNumber(20)
        val Status: List<TypeItem> = emptyList(),
        @ProtoNumber(21)
        val Authors: List<TypeItem> = emptyList(),
        @ProtoNumber(22)
        val SubscribeNum: Int? = null,
        @ProtoNumber(23)
        val Chapters: List<Chapter> = emptyList(),
        @ProtoNumber(24)
        val IsNeedLogin: Int? = null,
        @ProtoNumber(26)
        val IsHideChapter: Int? = null,
    ) {

        @Serializable
        data class TypeItem(
            @ProtoNumber(1)
            val TagId: Int,
            @ProtoNumber(2)
            val TagName: String,
        )

        @Serializable
        data class Chapter(
            @ProtoNumber(1)
            val Title: String,
            @ProtoNumber(2)
            val Data: List<ChapterInfo> = emptyList(),
        ) {

            @Serializable
            data class ChapterInfo(
                @ProtoNumber(1)
                val ChapterId: Int,
                @ProtoNumber(2)
                val ChapterTitle: String,
                @ProtoNumber(3)
                val Updatetime: Long,
                @ProtoNumber(4)
                val Filesize: Int = 0,
                @ProtoNumber(5)
                val ChapterOrder: Int = 0,
            )

        }

    }

}