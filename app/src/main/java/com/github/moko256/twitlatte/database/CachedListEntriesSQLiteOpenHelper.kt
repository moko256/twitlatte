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
import com.github.moko256.latte.client.base.entity.ListEntry
import java.io.File
import java.util.*

/**
 * Created by moko256 on 2019/01/02.
 *
 * @author moko256
 */

class CachedListEntriesSQLiteOpenHelper(
        context: Context,
        accessToken: AccessToken?,
        userId: Long
): SQLiteOpenHelper(
        context,
        if (accessToken != null) {
            File(context.cacheDir, "${accessToken.getKeyString()}/$userId/ListEntries.db").absolutePath
        } else {
            null
        },
        null,
        1
) {
    private companion object {
        private const val TABLE_NAME = "ListEntries"
        private val TABLE_COLUMNS = arrayOf("listId", "title", "description", "isPublic")
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("create table $TABLE_NAME(${TABLE_COLUMNS.joinToString(",")})")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

    fun getListEntries(): List<ListEntry> {
            var listEntries: MutableList<ListEntry>

            synchronized(this) {
                val database = readableDatabase
                val c = database.query(
                        TABLE_NAME,
                        TABLE_COLUMNS,
                        null, null, null, null, null)

                try {
                    listEntries = ArrayList(c.count)

                    while (c.moveToNext()) {
                        listEntries.add(ListEntry(
                                c.getLong(0),
                                c.getString(1),
                                c.getString(2),
                                c.getBoolean(3)
                        ))
                    }
                } catch (e: Throwable) {
                    listEntries = mutableListOf()
                    val writableDatabase = writableDatabase
                    writableDatabase.delete(TABLE_NAME, null, null)
                    writableDatabase.close()
                }

                c.close()
                database.close()
            }

            return listEntries
        }

        fun setListEntries(listEntries: List<ListEntry>) {
            synchronized(this) {
                val database = writableDatabase
                database.beginTransaction()
                database.delete(TABLE_NAME, null, null)

                listEntries.forEach {
                    val contentValues = ContentValues(4)
                    contentValues.put(TABLE_COLUMNS[0], it.listId)
                    contentValues.put(TABLE_COLUMNS[1], it.title)
                    contentValues.put(TABLE_COLUMNS[2], it.description)
                    contentValues.put(TABLE_COLUMNS[3], it.isPublic)

                    database.insert(TABLE_NAME, null, contentValues)
                }

                database.setTransactionSuccessful()
                database.endTransaction()
                database.close()
            }
        }
}
