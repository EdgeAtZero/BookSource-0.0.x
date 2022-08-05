@file:JvmName("Converters")

package io.github.edgeatzero.booksource.dmzj.utils

import io.github.edgeatzero.booksource.dmzj.models.*
import io.github.edgeatzero.booksource.models.*
import kotlinx.datetime.Instant

internal const val SPLIT_COMMA = ", "
internal const val SPLIT_SLASH = "/"
internal const val STRING_FINISHED = "\u5df2\u5b8c\u7ed3"
internal const val CHAR_FINISHED = '\u5b8c'

fun parseFetchBook(response: FetchProtoResponse): Book = SBook(
    id = response.Data.Id.toString(),
    title = response.Data.Title,
    authors = response.Data.Authors.map { it.TagName },
    status = if (response.Data.Status.any { it.TagName.contains("\u5df2\u5b8c\u7ed3") }) Book.Status.Finished else Book.Status.Ongoing,
    tags = response.Data.TypesTypes.map { STag(it.TagName) },
    imageUrl = response.Data.Cover,
    description = response.Data.Description,
    lastUpdated = Instant.fromEpochSeconds(response.Data.LastUpdatetime),
)

fun parseFetchBook(response: FetchJsonResponse): Book = SBook(
    id = response.data.info.id,
    title = response.data.info.title,
    authors = response.data.info.authors.split(SPLIT_COMMA),
    status = if (response.data.info.status.contains(STRING_FINISHED)) Book.Status.Finished else Book.Status.Ongoing,
    tags = response.data.info.types.split(SPLIT_SLASH).map { STag(it) },
    uploader = null,
    imageUrl = response.data.info.cover,
    description = response.data.info.description,
    lastUpdated = Instant.fromEpochSeconds(response.data.info.lastUpdatetime),
)

fun parseFetchBook(response: SearchJsonResponseItem): Book = SBook(
    id = response.id.toString(),
    title = response.title,
    authors = response.authors.split(SPLIT_COMMA),
    status = if (response.status.contains(CHAR_FINISHED)) Book.Status.Finished else Book.Status.Ongoing,
    imageUrl = response.cover,
)

fun parseFetchBook(response: SearchValResponseItem): Book = SBook(
    id = response.id.toString(),
    title = response.comicName,
    authors = response.comicAuthor.split(SPLIT_COMMA),
    status = if (response.status.contains(CHAR_FINISHED)) Book.Status.Finished else Book.Status.Ongoing,
    imageUrl = response.comicCover,
)

fun parseFetchChapter(response: FetchProtoResponse): List<Chapter> = response.Data.Chapters.flatMap { chapter ->
    chapter.Data.map {
        SChapter(
            id = it.ChapterId.toString(),
            name = "${chapter.Title}: ${it.ChapterTitle}",
            lastUpdated = Instant.fromEpochSeconds(it.Updatetime)
        )
    }
}

fun parseFetchChapter(response: FetchJsonResponse): List<Chapter> = response.data.list.map {
    SChapter(
        id = it.id,
        name = it.chapterName,
        lastUpdated = Instant.fromEpochSeconds(it.updatetime)
    )
}

fun parseContents(response: ContentsResponse): Contents = response.pageUrl.map {
    Content.Image(url = it)
}