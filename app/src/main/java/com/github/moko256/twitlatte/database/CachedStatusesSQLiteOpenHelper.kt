/*
 * Copyright 2015-2018 The twitlatte authors
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
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteStatement
import com.github.moko256.twitlatte.converter.convertToStatusOrRepeat
import com.github.moko256.twitlatte.database.migrator.OldCachedStatusesSQLiteOpenHelper
import com.github.moko256.twitlatte.entity.*
import com.github.moko256.twitlatte.text.link.convertHtmlToContentAndLinks
import com.github.moko256.twitlatte.text.link.entity.Link
import com.github.moko256.twitlatte.text.splitWithComma
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by moko256 on 2017/03/17.
 *
 * @author moko256
 */
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
        "visibility"
)

private const val COUNTS_TABLE_NAME = "Counts"

class CachedStatusesSQLiteOpenHelper(
        context: Context,
        val accessToken: AccessToken?
): SQLiteOpenHelper(
        context,
        if (accessToken != null) {
            File(context.cacheDir, accessToken.getKeyString() + "/CachedStatuses.db").absolutePath
        } else {
            null
        },
        null, 4
) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
                "create table $TABLE_NAME(${TABLE_COLUMNS.joinToString(",")}, primary key(id))"
        )
        db.execSQL("create unique index IdIndex on $TABLE_NAME(id)")

        db.execSQL(
                "create table $COUNTS_TABLE_NAME(id integer primary key,count integer default 0)"
        )
        db.execSQL("create unique index CountsIdIndex on $TABLE_NAME(id)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("alter table $TABLE_NAME add column contentWarning")
        }

        if (oldVersion < 3) {
            db.execSQL("alter table $TABLE_NAME add column repliesCount")
        }

        if (oldVersion < 4) {
            db.execSQL("alter table CachedStatuses rename to CachedStatusesOld")
            val oldStatuses = OldCachedStatusesSQLiteOpenHelper.getCachedStatus(db)

            db.execSQL(
                    "create table $TABLE_NAME(${TABLE_COLUMNS.joinToString(",")}, primary key(id))"
            )

            db.execSQL(
                    "create table $COUNTS_TABLE_NAME(id integer primary key,count integer default 0)"
            )
            db.execSQL("create unique index CountsIdIndex on $TABLE_NAME(id)")

            oldStatuses.forEach { status ->
                val contentValue = createStatusContentValues(if (accessToken?.type == ClientType.MASTODON) {

                    if (status.retweetedStatusId == -1L) {
                        val urls = status.text.convertHtmlToContentAndLinks()


                        val (sourceName, sourceWebsite) = if (
                                status.source.length > 8
                                && status.source.substring(0 .. 7) == "<a href="
                        ) {
                            val parsedSource = status.source.convertHtmlToContentAndLinks()

                            parsedSource.first to parsedSource.second?.first()?.url
                        } else {
                            status.source to null
                        }
                        //val sourceName = parsedSource.first
                        //val sourceWebsite = parsedSource.second.first().url
                        Status(
                                id = status.id,
                                userId = status.user.id,
                                text = urls.first,
                                sourceName = sourceName,
                                sourceWebsite = sourceWebsite,
                                createdAt = status.createdAt,
                                inReplyToStatusId = status.inReplyToStatusId,
                                inReplyToUserId = status.inReplyToUserId,
                                inReplyToScreenName = status.inReplyToScreenName,
                                isFavorited = status.isFavorited,
                                isRepeated = status.isRetweeted,
                                favoriteCount = status.favoriteCount,
                                repeatCount = status.retweetCount,
                                repliesCount = status.repliesCount,
                                isSensitive = status.isPossiblySensitive,
                                lang = status.lang,
                                medias = status.mediaEntities.takeIf { it.isNotEmpty() }?.map {
                                    val thumbnailUrl: String?
                                    val resultUrl: String
                                    val type: String

                                    when(it.type) {
                                        "video" -> {
                                            thumbnailUrl = it.mediaURLHttps
                                            resultUrl = it.videoVariants[0].url
                                            type = Media.ImageType.VIDEO_ONE.value
                                        }
                                        "animated_gif" -> {
                                            thumbnailUrl = it.mediaURLHttps
                                            resultUrl = it.videoVariants[0].url
                                            type = Media.ImageType.GIF.value
                                        }
                                        else -> {
                                            thumbnailUrl = null
                                            resultUrl = it.mediaURLHttps
                                            type = Media.ImageType.PICTURE.value
                                        }
                                    }

                                    Media(
                                            thumbnailUrl = thumbnailUrl,
                                            originalUrl = resultUrl,
                                            downloadVideoUrl = null,
                                            imageType = type
                                    )
                                }?.toTypedArray(),
                                urls = urls.second,
                                emojis = status.emojis?.toTypedArray(),
                                url = status.remoteUrl,
                                mentions = status.userMentionEntities.map {
                                    it.screenName
                                }.toTypedArray(),
                                spoilerText = status.spoilerText,
                                quotedStatusId = status.quotedStatusId,
                                visibility = null
                        )
                    } else {
                        Repeat(
                                id = status.id,
                                userId = status.user.id,
                                repeatedStatusId = status.retweetedStatusId,
                                createdAt = status.createdAt
                        )
                    }
                } else {
                    status.convertToStatusOrRepeat()
                })

                db.insert(TABLE_NAME, null, contentValue)

                db.execSQL("insert into $COUNTS_TABLE_NAME(id) values(${status.id})")
            }

            oldStatuses.close()

            db.execSQL("drop table CachedStatusesOld")
        }
    }

    fun getCachedStatus(id: Long): StatusObject? {
        var status: StatusObject? = null

        synchronized(this) {
            val database = readableDatabase
            val c = database.query(
                    TABLE_NAME,
                    TABLE_COLUMNS,
                    "id=" + id.toString(), null, null, null, null, "1"
            )
            if (c.moveToLast()) {
                val createdAt = Date(c.getLong(0))
                val statusId = c.getLong(1)
                val userId = c.getLong(2)
                val repeatedStatusId = c.getLong(3)
                if (repeatedStatusId == -1L) {
                    status = Status(
                            createdAt = createdAt,
                            id = statusId,
                            userId = userId,
                            text = c.getString(4),
                            sourceName = c.getString(5),
                            sourceWebsite = c.getString(6),
                            inReplyToStatusId = c.getLong(7),
                            inReplyToUserId = c.getLong(8),
                            isFavorited = c.getInt(9) != 0,
                            isRepeated = c.getInt(10) != 0,
                            favoriteCount = c.getInt(11),
                            repeatCount = c.getInt(12),
                            repliesCount = c.getInt(13),
                            inReplyToScreenName = c.getString(14),
                            isSensitive = c.getInt(15) != 0,
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
                            visibility = c.getString(30)
                    )
                } else {
                    status = Repeat(
                            id = statusId,
                            userId = userId,
                            createdAt = createdAt,
                            repeatedStatusId = repeatedStatusId
                    )
                }
            }

            c.close()
            database.close()
        }

        return status
    }

    fun getIdsInUse(ids: List<Long>): List<Long> {
        val result = ArrayList<Long>(ids.size * 5)

        synchronized(this) {
            val database = readableDatabase

            ids.forEach { id ->
                val c = database.query(
                        TABLE_NAME,
                        arrayOf(TABLE_COLUMNS[1], TABLE_COLUMNS[3], TABLE_COLUMNS[25]),
                        "id=" + id.toString(), null, null, null, null
                )

                while (c.moveToNext()) {
                    val repeatId = c.getLong(1)
                    val quotedId = c.getLong(2)
                    if (repeatId != -1L) {
                        if (!result.contains(repeatId) && !ids.contains(repeatId)) {
                            result.add(repeatId)
                        }
                    } else if (quotedId != -1L) {
                        if (!result.contains(quotedId) && !ids.contains(quotedId)) {
                            result.add(quotedId)
                        }
                    }
                }

                c.close()
            }

            database.close()
        }

        if (result.isNotEmpty()) {
            result.addAll(getIdsInUse(result))
        }
        return result
    }

    fun addCachedStatus(status: StatusObject, incrementCount: Boolean) {
        val values = createStatusContentValues(status)

        synchronized(this) {
            val database = writableDatabase
            database.beginTransaction()
            database.replace(TABLE_NAME, null, values)
            if (incrementCount) {
                database.execSQL("insert or ignore into $COUNTS_TABLE_NAME(id) values(${status.getId()})")

                val statement = incrementCountStatement(database)
                statement.bindLong(1, status.getId())
                statement.execute()
            }
            database.setTransactionSuccessful()
            database.endTransaction()
            database.close()
        }
    }

    fun addCachedStatuses(statuses: Collection<StatusObject>, incrementCount: Boolean, vararg excludeIncrementIds: Long) {
        val contentValues = ArrayList<ContentValues>(statuses.size)
        for (status in statuses) {
            contentValues.add(createStatusContentValues(status))
        }

        synchronized(this) {
            val database = writableDatabase
            database.beginTransaction()
            val statement = if (incrementCount) incrementCountStatement(database) else null
            for (values in contentValues) {
                database.replace(TABLE_NAME, null, values)

                val id = values.getAsLong(TABLE_COLUMNS[1])
                if (incrementCount && (excludeIncrementIds.isEmpty() || !excludeIncrementIds.contains(id))) {
                    database.execSQL("insert or ignore into $COUNTS_TABLE_NAME(id) values($id)")

                    statement!!.bindLong(1, id!!)
                    statement.execute()
                }
            }
            database.setTransactionSuccessful()
            database.endTransaction()
            database.close()
        }
    }

    private fun incrementCountStatement(database: SQLiteDatabase): SQLiteStatement {
        return database.compileStatement("UPDATE $COUNTS_TABLE_NAME SET count=count+1 WHERE id=?")
    }

    private fun createStatusContentValues(status: StatusObject): ContentValues {
        val contentValues = ContentValues(TABLE_COLUMNS.size)

        when(status) {
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

                if (status.mentions != null) {
                    contentValues.put(TABLE_COLUMNS[17], status.mentions.joinToString(","))
                }

                if (status.urls != null) {
                    val size = status.urls.size
                    val urls = arrayOfNulls<String>(size)
                    val starts = arrayOfNulls<String>(size)
                    val ends = arrayOfNulls<String>(size)

                    status.urls.forEachIndexed { i, entity ->
                        urls[i] = entity.url
                        starts[i] = entity.start.toString()
                        ends[i] = entity.end.toString()
                    }
                    contentValues.put(TABLE_COLUMNS[18], urls.joinToString(","))
                    contentValues.put(TABLE_COLUMNS[19], starts.joinToString(","))
                    contentValues.put(TABLE_COLUMNS[20], ends.joinToString(","))
                }

                if (status.medias != null) {
                    val size = status.medias.size
                    val thumbnailUrls = arrayOfNulls<String>(size)
                    val originalUrls = arrayOfNulls<String>(size)
                    val downloadVideoUrls = arrayOfNulls<String>(size)
                    val types = arrayOfNulls<String>(size)

                    status.medias.forEachIndexed { i, entity ->
                        thumbnailUrls[i] = entity.thumbnailUrl
                        originalUrls[i] = entity.originalUrl
                        downloadVideoUrls[i] = entity.downloadVideoUrl
                        types[i] = entity.imageType
                    }
                    contentValues.put(TABLE_COLUMNS[21], thumbnailUrls.joinToString(","))
                    contentValues.put(TABLE_COLUMNS[22], originalUrls.joinToString(","))
                    contentValues.put(TABLE_COLUMNS[23], downloadVideoUrls.joinToString(","))
                    contentValues.put(TABLE_COLUMNS[24], types.joinToString(","))
                }

                contentValues.put(TABLE_COLUMNS[25], status.quotedStatusId)

                contentValues.put(TABLE_COLUMNS[26], status.url)

                if (status.emojis != null) {
                    val size = status.emojis.size
                    val shortCodes = arrayOfNulls<String>(size)
                    val urls = arrayOfNulls<String>(size)

                    status.emojis.forEachIndexed { i, emoji ->
                        shortCodes[i] = emoji.shortCode
                        urls[i] = emoji.url
                    }
                    contentValues.put(TABLE_COLUMNS[27], shortCodes.joinToString(","))
                    contentValues.put(TABLE_COLUMNS[28], urls.joinToString(","))
                }
                contentValues.put(TABLE_COLUMNS[29], status.spoilerText)
                contentValues.put(TABLE_COLUMNS[30], status.visibility)
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
        synchronized(this) {
            val database = writableDatabase
            database.beginTransaction()
            val sqLiteStatement = decrementCountStatement(database)
            for (id in ids) {
                sqLiteStatement.bindLong(1, id)
                sqLiteStatement.execute()
            }
            database.delete(COUNTS_TABLE_NAME, "count=0", null)
            database.setTransactionSuccessful()
            database.endTransaction()
            database.close()
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
                    imageType = imageTypes[it]
            )
        }
    } else {
        null
    }
}

private fun String?.splitWithCommaAndReplaceEmptyWithNull(): List<String?>? {
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