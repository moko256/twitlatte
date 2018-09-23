package com.github.moko256.twitlatte.database.migrator

import android.content.ContentValues
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import com.github.moko256.twitlatte.array.toCommaSplitString
import com.github.moko256.twitlatte.text.link.MTHtmlParser
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

object OldCachedUserSQLiteOpenHelper {
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
                MTHtmlParser.convertToContentAndLinks(text)
            }
            val values = ContentValues(TABLE_COLUMNS.size)
            DatabaseUtils.cursorRowToContentValues(c, values)
            values.put("id", id)
            values.put("description", fixedText)
            values.put("urls_urls", links.map { it.url }.toTypedArray().toCommaSplitString().toString())
            values.put("urls_starts", links.map { it.start.toString() }.toTypedArray().toCommaSplitString().toString())
            values.put("urls_ends", links.map { it.end.toString() }.toTypedArray().toCommaSplitString().toString())
            db.replace(table_name, null, values)
        }
        c.close()
    }
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
                    return Integer.parseInt(starts!![i].trim { it <= ' ' })
                }

                override fun getEnd(): Int {
                    return Integer.parseInt(ends!![i].trim { it <= ' ' })
                }
            }
        }