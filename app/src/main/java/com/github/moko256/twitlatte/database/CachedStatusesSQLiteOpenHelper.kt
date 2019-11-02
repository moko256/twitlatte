/*
 * Copyright 2015-2019 The twitlatte authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.moko256.twitlatte.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteStatement
import androidx.collection.ArraySet
import com.github.moko256.latte.client.base.entity.*
import com.github.moko256.latte.html.entity.Link
import com.github.moko256.twitlatte.database.utils.*
import com.github.moko256.twitlatte.text.splitWithComma
import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.*

/**
 * Created by moko256 on 2017/03/17.
 *
 * @author moko256
 */

class CachedStatusesSQLiteOpenHelper(
        context: Context,
        val accessToken: AccessToken?
) : SQLiteOpenHelper(
        context,
        if (accessToken != null) {
            File(context.cacheDir, accessToken.getKeyString() + "/CachedStatuses.db").absolutePath
        } else {
            null
        },
        null, 7
) {

    private companion object {
        private const val TABLE_NAME = "CachedStatuses"
        private val TABLE_COLUMNS = arrayOf(
                "createdAt",
                "id",
                "userId",
                "repeatedStatusId",
                "text",
                "sourceName",
                "sourceWebsite",
                "inReplyToStatusId",
                "inReplyToUserId",
                "isFavorited",
                "isRepeated",
                "favoriteCount",
                "repeatCount",
                "repliesCount",
                "inReplyToScreenName",
                "isSensitive",
                "lang",
                "mentions",
                "urls_urls",
                "urls_starts",
                "urls_ends",
                "medias_thumbnail_urls",
                "medias_original_urls",
                "medias_download_video_urls",
                "medias_types",
                "quotedStatusId",
                "url",
                "emojis_shortcodes",
                "emojis_urls",
                "contentWarning",
                "visibility",
                "card_title",
                "card_description",
                "card_url",
                "card_image_url",
                "poll_id",
                "poll_expiresAt",
                "poll_expired",
                "poll_multiple",
                "poll_votesCount",
                "poll_optionTitles",
                "poll_optionCounts",
                "poll_voted"
        )

        private const val COUNTS_TABLE_NAME = "Counts"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.createTableWithUniqueIntKey(TABLE_NAME, TABLE_COLUMNS, 1 /*TABLE_COLUMNS.indexOf("id")*/)
        db.createTableWithUniqueIntKey(COUNTS_TABLE_NAME, arrayOf("id", "count integer default 0"), 0)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 4) {
            db.execSQL("drop table $TABLE_NAME")
            onCreate(db)
        }
        if (oldVersion < 5) {
            db.addColumn(TABLE_NAME, "card_title")
            db.addColumn(TABLE_NAME, "card_url")
        }
        if (oldVersion < 6) {
            db.addColumn(TABLE_NAME, "card_description", "\"\"")
            db.addColumn(TABLE_NAME, "card_image_url")
        }
        if (oldVersion < 7) {
            arrayOf(
                    "poll_expiresAt",
                    "poll_expired",
                    "poll_multiple",
                    "poll_votesCount",
                    "poll_optionTitles",
                    "poll_optionCounts",
                    "poll_voted"
            ).forEach {
                db.addColumn(TABLE_NAME, it)
            }
            db.addColumn(TABLE_NAME, "poll_id", "-1")
        }
    }

    fun getCachedStatus(id: Long): StatusObject? {
        return selectSingleOrNull(
                TABLE_NAME,
                TABLE_COLUMNS,
                "id=$id"
        ) {
            convertCursorToStatusObject(this)
        }
    }

    private fun convertCursorToStatusObject(c: Cursor): StatusObject {
        val createdAt = Date(c.getLong(0))
        val statusId = c.getLong(1)
        val userId = c.getLong(2)
        val repeatedStatusId = c.getLong(3)
        return if (repeatedStatusId == -1L) {
            Status(
                    createdAt = createdAt,
                    id = statusId,
                    userId = userId,
                    text = c.getString(4),
                    sourceName = c.getString(5),
                    sourceWebsite = c.getString(6),
                    inReplyToStatusId = c.getLong(7),
                    inReplyToUserId = c.getLong(8),
                    isFavorited = c.getBoolean(9),
                    isRepeated = c.getBoolean(10),
                    favoriteCount = c.getInt(11),
                    repeatCount = c.getInt(12),
                    repliesCount = c.getInt(13),
                    inReplyToScreenName = c.getString(14),
                    isSensitive = c.getBoolean(15),
                    lang = c.getString(16),
                    mentions = c.getString(17).splitWithComma()?.toTypedArray(),
                    urls = restoreLinks(
                            c.getString(18).splitWithComma(),
                            c.getString(19).splitWithComma(),
                            c.getString(20).splitWithComma()
                    ),
                    medias = restoreMedias(
                            c.getString(21).splitWithCommaAndReplaceEmptyWithNull(),
                            c.getString(22).splitWithComma(),
                            c.getString(23).splitWithCommaAndReplaceEmptyWithNull(),
                            c.getString(24).splitWithComma()
                    ),
                    quotedStatusId = c.getLong(25),
                    url = c.getString(26),
                    emojis = restoreEmojis(
                            c.getString(27).splitWithComma(),
                            c.getString(28).splitWithComma()
                    ),
                    spoilerText = c.getString(29),
                    visibility = c.getString(30),
                    card = c.let {
                        val title = it.getString(31)
                        val description = it.getString(32)
                        val url = it.getString(33)
                        val imageUrl = it.getString(34)

                        if (title != null && description != null && url != null) {
                            Card(
                                    title = title,
                                    description = description,
                                    url = url,
                                    imageUrl = imageUrl
                            )
                        } else {
                            null
                        }
                    },
                    poll = let {
                        val pollId = c.getLong(35)
                        if (pollId != -1L) {
                            Poll(
                                    pollId,
                                    Date(c.getLong(36)),
                                    c.getBoolean(37),
                                    c.getBoolean(38),
                                    c.getInt(39),
                                    c.getString(40).splitWithComma()?.map { URLDecoder.decode(it, "utf-8") }
                                            ?: emptyList(),
                                    c.getString(41).splitWithComma()?.map { it.toInt() }
                                            ?: emptyList(),
                                    c.getBoolean(42)
                            )
                        } else {
                            null
                        }
                    }
            )
        } else {
            Repeat(
                    id = statusId,
                    userId = userId,
                    createdAt = createdAt,
                    repeatedStatusId = repeatedStatusId
            )
        }
    }

    fun getIdsInUse(ids: Collection<Long>): Collection<Long> {
        val result = ArraySet<Long>(ids.size * 5)

        ids.forEach { id ->
            selectMultiple(
                    TABLE_NAME,
                    arrayOf(TABLE_COLUMNS[1], TABLE_COLUMNS[3], TABLE_COLUMNS[25]),
                    "id=$id"
            ) {
                val repeatId = getLong(1)
                val quotedId = getLong(2)
                if (repeatId != -1L) {
                    if (!ids.contains(repeatId)) {
                        result.add(repeatId)
                    }
                } else if (quotedId != -1L) {
                    if (!ids.contains(quotedId)) {
                        result.add(quotedId)
                    }
                }
            }
        }

        if (result.isNotEmpty()) {
            result.addAll(getIdsInUse(result))
        }
        return result
    }

    fun addCachedStatus(status: StatusObject, incrementCount: Boolean) {
        val values = createStatusContentValues(status)

        transaction {
            replace(TABLE_NAME, null, values)
            if (incrementCount) {
                val id = status.getId()
                val insertIfNeeded = insertOrIgnoreCountStatement(this)
                val increment = incrementCountStatement(this)

                insertIfNeeded.bindLong(1, id)
                insertIfNeeded.execute()

                increment.bindLong(1, id)
                increment.execute()
            }
        }
    }

    fun addCachedStatuses(statuses: Collection<StatusObject>, incrementCount: Boolean, vararg excludeIncrementIds: Long) {
        val contentValues = statuses.map { createStatusContentValues(it) }

        transaction {
            contentValues.forEach {
                replace(TABLE_NAME, null, it)
            }

            if (incrementCount) {
                val insertIfNeeded = insertOrIgnoreCountStatement(this)
                val increment = incrementCountStatement(this)

                statuses.forEach {
                    val id = it.getId()
                    if (excludeIncrementIds.isEmpty() || !excludeIncrementIds.contains(id)) {
                        insertIfNeeded.bindLong(1, id)
                        insertIfNeeded.execute()

                        increment.bindLong(1, id)
                        increment.execute()
                    }
                }
            }
        }
    }

    private fun insertOrIgnoreCountStatement(database: SQLiteDatabase): SQLiteStatement {
        return database.compileStatement("insert or ignore into $COUNTS_TABLE_NAME(id) values(?)")
    }

    private fun incrementCountStatement(database: SQLiteDatabase): SQLiteStatement {
        return database.compileStatement("UPDATE $COUNTS_TABLE_NAME SET count=count+1 WHERE id=?")
    }

    private fun createStatusContentValues(status: StatusObject): ContentValues {
        val contentValues = ContentValues(TABLE_COLUMNS.size)

        when (status) {
            is Status -> {
                contentValues.put(TABLE_COLUMNS[0], status.createdAt.time)
                contentValues.put(TABLE_COLUMNS[1], status.id)
                contentValues.put(TABLE_COLUMNS[2], status.userId)
                contentValues.put(TABLE_COLUMNS[3], -1L)
                contentValues.put(TABLE_COLUMNS[4], status.text)
                contentValues.put(TABLE_COLUMNS[5], status.sourceName)
                contentValues.put(TABLE_COLUMNS[6], status.sourceWebsite)
                contentValues.put(TABLE_COLUMNS[7], status.inReplyToStatusId)
                contentValues.put(TABLE_COLUMNS[8], status.inReplyToUserId)
                contentValues.put(TABLE_COLUMNS[9], status.isFavorited)
                contentValues.put(TABLE_COLUMNS[10], status.isRepeated)
                contentValues.put(TABLE_COLUMNS[11], status.favoriteCount)
                contentValues.put(TABLE_COLUMNS[12], status.repeatCount)
                contentValues.put(TABLE_COLUMNS[13], status.repliesCount)
                contentValues.put(TABLE_COLUMNS[14], status.inReplyToScreenName)
                contentValues.put(TABLE_COLUMNS[15], status.isSensitive)
                contentValues.put(TABLE_COLUMNS[16], status.lang)

                val mentions = status.mentions
                if (mentions != null) {
                    contentValues.put(TABLE_COLUMNS[17], mentions.joinToString(","))
                }

                status.urls?.let {
                    val size = it.size
                    val urls = arrayOfNulls<String>(size)
                    val starts = arrayOfNulls<String>(size)
                    val ends = arrayOfNulls<String>(size)

                    it.forEachIndexed { i, entity ->
                        urls[i] = entity.url
                        starts[i] = entity.start.toString()
                        ends[i] = entity.end.toString()
                    }
                    contentValues.put(TABLE_COLUMNS[18], urls.joinToString(","))
                    contentValues.put(TABLE_COLUMNS[19], starts.joinToString(","))
                    contentValues.put(TABLE_COLUMNS[20], ends.joinToString(","))
                }

                status.medias?.let {
                    val size = it.size
                    val thumbnailUrls = arrayOfNulls<String>(size)
                    val originalUrls = arrayOfNulls<String>(size)
                    val downloadVideoUrls = arrayOfNulls<String>(size)
                    val types = arrayOfNulls<String>(size)

                    it.forEachIndexed { i, entity ->
                        thumbnailUrls[i] = entity.thumbnailUrl
                        originalUrls[i] = entity.originalUrl
                        downloadVideoUrls[i] = entity.downloadVideoUrl
                        types[i] = entity.mediaType
                    }
                    contentValues.put(TABLE_COLUMNS[21], thumbnailUrls.joinToString(","))
                    contentValues.put(TABLE_COLUMNS[22], originalUrls.joinToString(","))
                    contentValues.put(TABLE_COLUMNS[23], downloadVideoUrls.joinToString(","))
                    contentValues.put(TABLE_COLUMNS[24], types.joinToString(","))
                }

                contentValues.put(TABLE_COLUMNS[25], status.quotedStatusId)

                contentValues.put(TABLE_COLUMNS[26], status.url)

                status.emojis?.let {
                    val size = it.size
                    val shortCodes = arrayOfNulls<String>(size)
                    val urls = arrayOfNulls<String>(size)

                    it.forEachIndexed { i, emoji ->
                        shortCodes[i] = emoji.shortCode
                        urls[i] = emoji.url
                    }
                    contentValues.put(TABLE_COLUMNS[27], shortCodes.joinToString(","))
                    contentValues.put(TABLE_COLUMNS[28], urls.joinToString(","))
                }

                contentValues.put(TABLE_COLUMNS[29], status.spoilerText)
                contentValues.put(TABLE_COLUMNS[30], status.visibility)

                status.card?.let {
                    contentValues.put(TABLE_COLUMNS[31], it.title)
                    contentValues.put(TABLE_COLUMNS[32], it.description)
                    contentValues.put(TABLE_COLUMNS[33], it.url)
                    contentValues.put(TABLE_COLUMNS[34], it.imageUrl)
                }

                val poll = status.poll
                if (poll != null) {
                    contentValues.put(TABLE_COLUMNS[35], poll.id)
                    contentValues.put(TABLE_COLUMNS[36], poll.expiresAt?.time)
                    contentValues.put(TABLE_COLUMNS[37], poll.expired)
                    contentValues.put(TABLE_COLUMNS[38], poll.multiple)
                    contentValues.put(TABLE_COLUMNS[39], poll.votesCount)
                    contentValues.put(TABLE_COLUMNS[40], poll.optionTitles.joinToString(",") { URLEncoder.encode(it, "utf-8") })
                    contentValues.put(TABLE_COLUMNS[41], poll.optionCounts.joinToString(","))
                    contentValues.put(TABLE_COLUMNS[42], poll.voted)
                } else {
                    contentValues.put(TABLE_COLUMNS[35], -1L)
                }
            }

            is Repeat -> {
                contentValues.put(TABLE_COLUMNS[0], status.createdAt.time)
                contentValues.put(TABLE_COLUMNS[1], status.id)
                contentValues.put(TABLE_COLUMNS[2], status.userId)
                contentValues.put(TABLE_COLUMNS[3], status.repeatedStatusId)
            }
        }

        return contentValues
    }

    fun deleteCachedStatuses(ids: Collection<Long>) {
        transaction {
            val sqLiteStatement = decrementCountStatement(this)
            for (id in ids) {
                sqLiteStatement.bindLong(1, id)
                sqLiteStatement.execute()
            }
            delete(TABLE_NAME, "id not in (select id from $COUNTS_TABLE_NAME where count>0)", null)
            delete(COUNTS_TABLE_NAME, "count=0", null)
        }
    }

    private fun decrementCountStatement(database: SQLiteDatabase): SQLiteStatement {
        return database.compileStatement("UPDATE $COUNTS_TABLE_NAME SET count=count-1 WHERE id=?")
    }

    private fun restoreEmojis(
            shortCodes: List<String>?,
            urls: List<String>?
    ): Array<Emoji>? = if (shortCodes != null && urls != null && urls.size == shortCodes.size) {
        Array(shortCodes.size) {
            Emoji(
                    shortCode = shortCodes[it],
                    url = urls[it]
            )
        }
    } else {
        null
    }

    private fun restoreLinks(
            urls: List<String>?,
            starts: List<String>?,
            ends: List<String>?
    ): Array<Link>? = if (urls != null
            && starts != null
            && starts.size == urls.size
            && ends != null
            && ends.size == urls.size) {
        Array(urls.size) {
            Link(
                    url = urls[it],
                    start = starts[it].toInt(),
                    end = ends[it].toInt()
            )
        }
    } else {
        null
    }

    private fun restoreMedias(
            thumbnailUrls: List<String?>?,
            originalUrls: List<String>?,
            downloadVideoUrls: List<String?>?,
            imageTypes: List<String>?
    ): Array<Media>? = if (imageTypes != null
            && thumbnailUrls != null && thumbnailUrls.size == imageTypes.size
            && originalUrls != null && originalUrls.size == imageTypes.size
            && downloadVideoUrls != null && downloadVideoUrls.size == imageTypes.size) {
        Array(imageTypes.size) {
            Media(
                    thumbnailUrl = thumbnailUrls[it],
                    originalUrl = originalUrls[it],
                    downloadVideoUrl = downloadVideoUrls[it],
                    mediaType = imageTypes[it]
            )
        }
    } else {
        null
    }
}

internal fun String?.splitWithCommaAndReplaceEmptyWithNull(): List<String?>? {
    return if (this != null && isNotEmpty()) {
        this.split(",")
                .map {
                    if (it == "null") {
                        null
                    } else {
                        it
                    }
                }
    } else {
        null
    }
}