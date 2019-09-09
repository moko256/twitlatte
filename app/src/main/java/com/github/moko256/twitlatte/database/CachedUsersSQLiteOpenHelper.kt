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
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.github.moko256.latte.client.base.entity.AccessToken
import com.github.moko256.latte.client.base.entity.Emoji
import com.github.moko256.latte.client.base.entity.User
import com.github.moko256.latte.html.entity.Link
import com.github.moko256.twitlatte.text.splitWithComma
import java.io.File
import java.util.*

/**
 * Created by moko256 on 2017/03/17.
 *
 * @author moko256
 */

class CachedUsersSQLiteOpenHelper(context: Context, accessToken: AccessToken?) : SQLiteOpenHelper(context, if (accessToken != null) File(context.cacheDir, accessToken.getKeyString() + "/" + "CachedUsers.db").absolutePath else null, null, 3) {

    private companion object {
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
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.createTableWithUniqueIntKey(TABLE_NAME, TABLE_COLUMNS, 0 /*TABLE_COLUMNS.indexOf("id")*/)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            db.execSQL("drop table CachedStatuses")

            onCreate(db)
        }
    }

    fun getCachedUser(id: Long): User? {
        var user: User? = null

        val c = readableDatabase.query(
                TABLE_NAME,
                TABLE_COLUMNS,
                "id=$id", null, null, null, null, "1"
        )
        if (c.moveToLast()) {
            user = User(
                    id = c.getLong(0),
                    name = c.getString(1),
                    screenName = c.getString(2),
                    location = c.getString(3),
                    description = c.getString(4),
                    isContributorsEnabled = c.getBoolean(5),
                    profileImageURLHttps = c.getString(6),
                    isDefaultProfileImage = c.getBoolean(7),
                    url = c.getString(8),
                    isProtected = c.getBoolean(9),
                    followersCount = c.getInt(10),
                    profileBackgroundColor = c.getString(11),
                    profileTextColor = c.getString(12),
                    profileLinkColor = c.getString(13),
                    profileSidebarFillColor = c.getString(14),
                    profileSidebarBorderColor = c.getString(15),
                    isProfileUseBackgroundImage = c.getBoolean(16),
                    isDefaultProfile = c.getBoolean(17),
                    favoritesCount = c.getInt(18),
                    friendsCount = c.getInt(19),
                    createdAt = Date(c.getLong(20)),
                    utcOffset = c.getInt(21),
                    timeZone = c.getString(22),
                    profileBackgroundImageURLHttps = c.getString(23),
                    profileBannerImageUrl = c.getString(24),
                    isProfileBackgroundTiled = c.getBoolean(25),
                    lang = c.getString(26),
                    statusesCount = c.getInt(27),
                    isVerified = c.getBoolean(28),
                    isTranslator = c.getBoolean(29),
                    isFollowRequestSent = c.getBoolean(30),
                    descriptionLinks = restoreLinks(
                            c.getString(31).splitWithComma(),
                            c.getString(32).splitWithComma(),
                            c.getString(33).splitWithComma()
                    ),
                    emojis = restoreEmojis(
                            c.getString(34).splitWithComma(),
                            c.getString(35).splitWithComma()
                    )
            )
        }

        c.close()

        return user
    }

    fun addCachedUser(user: User) {
        transaction {
            addCachedUserAtTransaction(this, user)
        }
    }

    fun addCachedUsers(users: Collection<User>) {
        transaction {
            for (user in users) {
                addCachedUserAtTransaction(this, user)
            }
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

        val descriptionLinks = user.descriptionLinks
        if (descriptionLinks != null) {
            val size = descriptionLinks.size
            val urls = arrayOfNulls<String>(size)
            val starts = arrayOfNulls<String>(size)
            val ends = arrayOfNulls<String>(size)

            descriptionLinks.forEachIndexed { i, entity ->
                urls[i] = entity.url
                starts[i] = entity.start.toString()
                ends[i] = entity.end.toString()
            }
            contentValues.put(TABLE_COLUMNS[31], urls.joinToString(","))
            contentValues.put(TABLE_COLUMNS[32], starts.joinToString(","))
            contentValues.put(TABLE_COLUMNS[33], ends.joinToString(","))
        }

        val emojis = user.emojis
        if (emojis != null) {
            val listSize = emojis.size
            val shortCodes = arrayOfNulls<String>(listSize)
            val urls = arrayOfNulls<String>(listSize)

            emojis.forEachIndexed { i, emoji ->
                shortCodes[i] = emoji.shortCode
                urls[i] = emoji.url
            }
            contentValues.put(TABLE_COLUMNS[34], shortCodes.joinToString(","))
            contentValues.put(TABLE_COLUMNS[35], urls.joinToString(","))
        }

        database.replace(TABLE_NAME, null, contentValues)
    }


    fun deleteCachedUser(id: Long) {
        write {
            delete(TABLE_NAME, "id=$id", null)
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