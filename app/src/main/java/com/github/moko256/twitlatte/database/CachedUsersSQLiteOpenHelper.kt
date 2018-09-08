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
import com.github.moko256.twitlatte.BuildConfig
import com.github.moko256.twitlatte.array.ArrayUtils
import com.github.moko256.twitlatte.entity.AccessToken
import com.github.moko256.twitlatte.entity.Emoji
import com.github.moko256.twitlatte.entity.Type
import com.github.moko256.twitlatte.entity.User
import twitter4j.URLEntity
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
        "URLEntity_texts",
        "URLEntity_URLs",
        "URLEntity_expandedURLs",
        "URLEntity_displayURLs",
        "URLEntity_starts",
        "URLEntity_ends",
        "Emoji_shortcodes",
        "Emoji_urls"
)

class CachedUsersSQLiteOpenHelper(context: Context, accessToken: AccessToken?) : SQLiteOpenHelper(context, if (accessToken != null) File(context.cacheDir, accessToken.getKeyString() + "/" + "CachedUsers.db").absolutePath else null, null, BuildConfig.CACHE_DATABASE_VERSION) {

    val isTwitter = accessToken?.type == Type.TWITTER

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
                "create table " + TABLE_NAME + "(" + ArrayUtils.toCommaSplitString(TABLE_COLUMNS) + ", primary key(id))"
        )
        db.execSQL("create unique index idindex on $TABLE_NAME(id)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion == 1) {
            db.execSQL("alter table " + TABLE_NAME + " add column " + TABLE_COLUMNS[43])
            db.execSQL("alter table " + TABLE_NAME + " add column " + TABLE_COLUMNS[44])
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
                        friendsCount = c.getInt(18),
                        createdAt = Date(c.getLong(19)),
                        favoritesCount = c.getInt(20),
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
                        descriptionURLEntities = restoreURLEntities(
                                c.getString(31).split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray(),
                                c.getString(32).split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray(),
                                c.getString(33).split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray(),
                                c.getString(34).split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray(),
                                c.getString(35).split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray(),
                                c.getString(36).split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        ),
                        emojis = restoreEmojis(
                                c.getString(37),
                                c.getString(38)
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
            addCachedUserAtTransaction(user)
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
                addCachedUserAtTransaction(user)
            }
            database.setTransactionSuccessful()
            database.endTransaction()
            database.close()
        }
    }

    private fun addCachedUserAtTransaction(user: User) {
        val database = writableDatabase

        val contentValues = ContentValues()
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

        val size = user.descriptionURLEntities.size
        val texts = arrayOfNulls<String>(size)
        val URLs = arrayOfNulls<String>(size)
        val expandedURLs = arrayOfNulls<String>(size)
        val displaysURLs = arrayOfNulls<String>(size)
        val starts = arrayOfNulls<String>(size)
        val ends = arrayOfNulls<String>(size)
        for (i in 0 until size) {
            val entity = user.descriptionURLEntities[i]
            texts[i] = entity.text
            URLs[i] = entity.url
            expandedURLs[i] = entity.expandedURL
            displaysURLs[i] = entity.displayURL
            starts[i] = entity.start.toString()
            ends[i] = entity.end.toString()
        }
        contentValues.put(TABLE_COLUMNS[31], ArrayUtils.toCommaSplitString(texts).toString())
        contentValues.put(TABLE_COLUMNS[32], ArrayUtils.toCommaSplitString(URLs).toString())
        contentValues.put(TABLE_COLUMNS[33], ArrayUtils.toCommaSplitString(expandedURLs).toString())
        contentValues.put(TABLE_COLUMNS[34], ArrayUtils.toCommaSplitString(displaysURLs).toString())
        contentValues.put(TABLE_COLUMNS[35], ArrayUtils.toCommaSplitString(starts).toString())
        contentValues.put(TABLE_COLUMNS[36], ArrayUtils.toCommaSplitString(ends).toString())

        if (user.emojis != null) {
            val listSize = user.emojis.size
            val shortcodes = arrayOfNulls<String>(listSize)
            val urls = arrayOfNulls<String>(listSize)

            for (i in 0 until listSize) {
                val emoji = user.emojis[i]
                shortcodes[i] = emoji.shortCode
                urls[i] = emoji.url
            }
            contentValues.put(TABLE_COLUMNS[37], ArrayUtils.toCommaSplitString(shortcodes).toString())
            contentValues.put(TABLE_COLUMNS[38], ArrayUtils.toCommaSplitString(urls).toString())
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


    private fun restoreURLEntities(texts: Array<String?>,
                                   URLs: Array<String?>,
                                   expandedURLs: Array<String?>,
                                   displaysURLs: Array<String?>,
                                   starts: Array<String?>,
                                   ends: Array<String?>): Array<URLEntity> =
            Array(texts.size){ i ->
                object : URLEntity {
                    override fun getText(): String {
                        return texts[i]!!
                    }

                    override fun getURL(): String {
                        return URLs[i]!!
                    }

                    override fun getExpandedURL(): String {
                        return expandedURLs[i]!!
                    }

                    override fun getDisplayURL(): String {
                        return displaysURLs[i]!!
                    }

                    override fun getStart(): Int {
                        return Integer.parseInt(starts[i]!!.trim { it <= ' ' })
                    }

                    override fun getEnd(): Int {
                        return Integer.parseInt(ends[i]!!.trim { it <= ' ' })
                    }
                }
            }

    private fun restoreEmojis(shortCodesString: String?,
                              urlsString: String?): List<Emoji>? =
            if (shortCodesString != null && urlsString != null) {
                val shortCodes = shortCodesString.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val urls = urlsString.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                val emojis = ArrayList<Emoji>(shortCodes.size)
                for (i in shortCodes.indices) {
                    emojis.add(Emoji(shortCodes[i], urls[i]))
                }

                emojis
            } else {
                null
            }
}