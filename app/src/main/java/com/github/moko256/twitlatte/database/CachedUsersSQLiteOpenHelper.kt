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
import com.github.moko256.twitlatte.database.migrator.migrateV2toV3
import com.github.moko256.twitlatte.entity.AccessToken
import com.github.moko256.twitlatte.entity.ClientType
import com.github.moko256.twitlatte.entity.Emoji
import com.github.moko256.twitlatte.entity.User
import com.github.moko256.twitlatte.text.link.entity.Link
import com.github.moko256.twitlatte.text.splitWithComma
import java.io.File
import java.util.*

/**
 * Created by moko256 on 2017/03/17.
 *
 * @author moko256
 */
private const val TABLE_NAME = "CachedUsers"
private val TABLE_COLUMNS = arrayOf(
        "id",
        "name",
        "screenName",
        "location",
        "description",
        "isContributorsEnabled",
        "profileImageURLHttps",
        "isDefaultProfileImage",
        "url",
        "isProtected",
        "followersCount",
        "profileBackgroundColor",
        "profileTextColor",
        "profileLinkColor",
        "profileSidebarFillColor",
        "profileSidebarBorderColor",
        "isProfileUseBackgroundImage",
        "isDefaultProfile",
        "friendsCount",
        "createdAt",
        "favoritesCount",
        "utcOffset",
        "timeZone",
        "profileBackgroundImageURLHttps",
        "profileBannerImageUrl",
        "isProfileBackgroundTiled",
        "lang",
        "statusesCount",
        "isVerified",
        "isTranslator",
        "isFollowRequestSent",
        "urls_urls",
        "urls_starts",
        "urls_ends",
        "Emoji_shortcodes",
        "Emoji_urls"
)

class CachedUsersSQLiteOpenHelper(context: Context, accessToken: AccessToken?) : SQLiteOpenHelper(context, if (accessToken != null) File(context.cacheDir, accessToken.getKeyString() + "/" + "CachedUsers.db").absolutePath else null, null, 3) {

    private val isTwitter = accessToken?.type == ClientType.TWITTER

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
                "create table " + TABLE_NAME + "(" + TABLE_COLUMNS.joinToString(",") + ", primary key(id))"
        )
        db.execSQL("create unique index idindex on $TABLE_NAME(id)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("alter table $TABLE_NAME add column Emoji_shortcodes")
            db.execSQL("alter table $TABLE_NAME add column Emoji_urls")
        }

        if (oldVersion < 3) {
            db.execSQL("alter table $TABLE_NAME add column urls_urls")
            db.execSQL("alter table $TABLE_NAME add column urls_starts")
            db.execSQL("alter table $TABLE_NAME add column urls_ends")
            migrateV2toV3(isTwitter, TABLE_NAME, db)
        }
    }

    fun getCachedUser(id: Long): User? {
        var user: User? = null
        synchronized(this) {
            val database = readableDatabase
            val c = database.query(
                    TABLE_NAME,
                    TABLE_COLUMNS,
                    "id=" + id.toString(), null, null, null, null, "1"
            )
            if (c.moveToLast()) {
                user = User(
                        id = c.getLong(0),
                        name = c.getString(1),
                        screenName = c.getString(2),
                        location = c.getString(3),
                        description = c.getString(4),
                        isContributorsEnabled = c.getInt(5) != 0,
                        profileImageURLHttps = c.getString(6),
                        isDefaultProfileImage = c.getInt(7) != 0,
                        url = c.getString(8),
                        isProtected = c.getInt(9) != 0,
                        followersCount = c.getInt(10),
                        profileBackgroundColor = c.getString(11),
                        profileTextColor = c.getString(12),
                        profileLinkColor = c.getString(13),
                        profileSidebarFillColor = c.getString(14),
                        profileSidebarBorderColor = c.getString(15),
                        isProfileUseBackgroundImage = c.getInt(16) != 0,
                        isDefaultProfile = c.getInt(17) != 0,
                        favoritesCount = c.getInt(18),
                        friendsCount = c.getInt(19),
                        createdAt = Date(c.getLong(20)),
                        utcOffset = c.getInt(21),
                        timeZone = c.getString(22),
                        profileBackgroundImageURLHttps = c.getString(23),
                        profileBannerImageUrl = c.getString(24),
                        isProfileBackgroundTiled = c.getInt(25) != 0,
                        lang = c.getString(26),
                        statusesCount = c.getInt(27),
                        isVerified = c.getInt(28) != 0,
                        isTranslator = c.getInt(29) != 0,
                        isFollowRequestSent = c.getInt(30) != 0,
                        descriptionLinks = restoreLinks(
                                c.getString(31).splitWithComma(),
                                c.getString(32).splitWithComma(),
                                c.getString(33).splitWithComma()
                        ),
                        emojis = restoreEmojis(
                                c.getString(34).splitWithComma(),
                                c.getString(35).splitWithComma()
                        ),
                        isTwitter = isTwitter
                )
            }

            c.close()
            database.close()
        }
        return user
    }

    fun addCachedUser(user: User) {
        synchronized(this) {
            val database = writableDatabase
            database.beginTransaction()
            addCachedUserAtTransaction(database, user)
            database.setTransactionSuccessful()
            database.endTransaction()
            database.close()
        }
    }

    fun addCachedUsers(users: Collection<User>) {
        synchronized(this) {
            val database = writableDatabase
            database.beginTransaction()
            for (user in users) {
                addCachedUserAtTransaction(database, user)
            }
            database.setTransactionSuccessful()
            database.endTransaction()
            database.close()
        }
    }

    private fun addCachedUserAtTransaction(database: SQLiteDatabase, user: User) {
        val contentValues = ContentValues(TABLE_COLUMNS.size)
        contentValues.put(TABLE_COLUMNS[0], user.id)
        contentValues.put(TABLE_COLUMNS[1], user.name)
        contentValues.put(TABLE_COLUMNS[2], user.screenName)
        contentValues.put(TABLE_COLUMNS[3], user.location)
        contentValues.put(TABLE_COLUMNS[4], user.description)
        contentValues.put(TABLE_COLUMNS[5], user.isContributorsEnabled)
        contentValues.put(TABLE_COLUMNS[6], user.profileImageURLHttps)
        contentValues.put(TABLE_COLUMNS[7], user.isDefaultProfileImage)
        contentValues.put(TABLE_COLUMNS[8], user.url)
        contentValues.put(TABLE_COLUMNS[9], user.isProtected)
        contentValues.put(TABLE_COLUMNS[10], user.followersCount)
        contentValues.put(TABLE_COLUMNS[11], user.profileBackgroundColor)
        contentValues.put(TABLE_COLUMNS[12], user.profileTextColor)
        contentValues.put(TABLE_COLUMNS[13], user.profileLinkColor)
        contentValues.put(TABLE_COLUMNS[14], user.profileSidebarFillColor)
        contentValues.put(TABLE_COLUMNS[15], user.profileSidebarBorderColor)
        contentValues.put(TABLE_COLUMNS[16], user.isProfileUseBackgroundImage)
        contentValues.put(TABLE_COLUMNS[17], user.isDefaultProfile)
        contentValues.put(TABLE_COLUMNS[18], user.favoritesCount)
        contentValues.put(TABLE_COLUMNS[19], user.friendsCount)
        contentValues.put(TABLE_COLUMNS[20], user.createdAt.time)
        contentValues.put(TABLE_COLUMNS[21], user.utcOffset)
        contentValues.put(TABLE_COLUMNS[22], user.timeZone)
        contentValues.put(TABLE_COLUMNS[23], user.profileBackgroundImageURLHttps)
        contentValues.put(TABLE_COLUMNS[24], user.profileBannerImageUrl)
        contentValues.put(TABLE_COLUMNS[25], user.isProfileBackgroundTiled)
        contentValues.put(TABLE_COLUMNS[26], user.lang)
        contentValues.put(TABLE_COLUMNS[27], user.statusesCount)
        contentValues.put(TABLE_COLUMNS[28], user.isVerified)
        contentValues.put(TABLE_COLUMNS[29], user.isTranslator)
        contentValues.put(TABLE_COLUMNS[30], user.isFollowRequestSent)

        if (user.descriptionLinks != null) {
            val size = user.descriptionLinks.size
            val urls = arrayOfNulls<String>(size)
            val starts = arrayOfNulls<String>(size)
            val ends = arrayOfNulls<String>(size)

            user.descriptionLinks.forEachIndexed { i, entity ->
                urls[i] = entity.url
                starts[i] = entity.start.toString()
                ends[i] = entity.end.toString()
            }
            contentValues.put(TABLE_COLUMNS[31], urls.joinToString(","))
            contentValues.put(TABLE_COLUMNS[32], starts.joinToString(","))
            contentValues.put(TABLE_COLUMNS[33], ends.joinToString(","))
        }

        if (user.emojis != null) {
            val listSize = user.emojis.size
            val shortCodes = arrayOfNulls<String>(listSize)
            val urls = arrayOfNulls<String>(listSize)

            user.emojis.forEachIndexed { i, emoji ->
                shortCodes[i] = emoji.shortCode
                urls[i] = emoji.url
            }
            contentValues.put(TABLE_COLUMNS[34], shortCodes.joinToString(","))
            contentValues.put(TABLE_COLUMNS[35], urls.joinToString(","))
        }

        database.replace(TABLE_NAME, null, contentValues)
    }


    fun deleteCachedUser(id: Long) {
        synchronized(this) {
            val database = writableDatabase
            database.delete(TABLE_NAME, "id=" + id.toString(), null)
            database.close()
        }
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

    private fun restoreEmojis(shortCodes: List<String>?,
                              urls: List<String>?): Array<Emoji>? =
            if (shortCodes != null && urls != null) {
                Array(shortCodes.size) {
                    Emoji(shortCodes[it], urls[it])
                }
            } else {
                null
            }
}