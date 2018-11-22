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

package com.github.moko256.twitlatte.database.migrator

import android.content.ContentValues
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import com.github.moko256.twitlatte.text.link.convertHtmlToContentAndLinks
import com.github.moko256.twitlatte.text.link.convertToContentAndLinks
import com.github.moko256.twitlatte.text.splitWithComma
import twitter4j.URLEntity

/**
 * Created by moko256 on 2018/09/12.
 *
 * @author moko256
 */
private val TABLE_COLUMNS = arrayOf(
        "id",
        "description",
        "URLEntity_texts",
        "URLEntity_URLs",
        "URLEntity_expandedURLs",
        "URLEntity_displayURLs",
        "URLEntity_starts",
        "URLEntity_ends",
        "name",
        "screenName",
        "location",
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
        "Emoji_shortcodes",
        "Emoji_urls"
)

fun migrateV2toV3(isTwitter: Boolean, table_name: String, db: SQLiteDatabase) {
    val c = db.query(
            table_name,
            TABLE_COLUMNS,
            null, null, null, null, null
    )
    while (c.moveToNext()) {
        val id = c.getLong(0)
        val text = c.getString(1)
        val entity = restoreURLEntities(
                c.getString(2).splitWithComma(),
                c.getString(3).splitWithComma(),
                c.getString(4).splitWithComma(),
                c.getString(5).splitWithComma(),
                c.getString(6).splitWithComma(),
                c.getString(7).splitWithComma()
        )
        val (fixedText, links) = if (isTwitter) {
            convertToContentAndLinks(
                    text,
                    emptyArray(),
                    emptyArray(),
                    emptyArray(),
                    emptyArray(),
                    entity
            )
        } else {
            text.convertHtmlToContentAndLinks()
        }
        val values = ContentValues(TABLE_COLUMNS.size)
        DatabaseUtils.cursorRowToContentValues(c, values)
        values.put("id", id)
        values.put("description", fixedText)
        values.put("urls_urls", links?.joinToString(",") { it.url }?:"")
        values.put("urls_starts", links?.joinToString(",") { it.start.toString() }?:"")
        values.put("urls_ends", links?.joinToString(",") { it.end.toString() }?:"")
        db.replace(table_name, null, values)
    }
    c.close()
}

private fun restoreURLEntities(texts: List<String>?,
                               URLs: List<String>?,
                               expandedURLs: List<String>?,
                               displaysURLs: List<String>?,
                               starts: List<String>?,
                               ends: List<String>?): Array<URLEntity> =
        Array(texts?.size?:0){ i ->
            object : URLEntity {
                override fun getText(): String {
                    return texts!![i]
                }

                override fun getURL(): String {
                    return URLs!![i]
                }

                override fun getExpandedURL(): String {
                    return expandedURLs!![i]
                }

                override fun getDisplayURL(): String {
                    return displaysURLs!![i]
                }

                override fun getStart(): Int {
                    return starts!![i].trim { it <= ' ' }.toInt()
                }

                override fun getEnd(): Int {
                    return ends!![i].trim { it <= ' ' }.toInt()
                }
            }
        }