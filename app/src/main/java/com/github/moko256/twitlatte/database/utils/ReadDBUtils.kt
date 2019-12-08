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

package com.github.moko256.twitlatte.database.utils

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Created by moko256 on 2019/09/11.
 *
 * @author moko256
 */

fun Cursor.getBoolean(index: Int) = getInt(index) == 1

inline fun <T> SQLiteOpenHelper.read(action: SQLiteDatabase.() -> T): T {
    val db = readableDatabase
    val result = action(db)
    db.close()
    return result
}

inline fun <T> SQLiteDatabase.selectSingleOrNull(
    tableName: String,
    columns: Array<String>,
    selection: String,
    action: Cursor.() -> T
): T? {
    val cursor = query(
        tableName,
        columns,
        selection,
        null,
        null,
        null,
        null,
        "1"
    )
    val result = if (cursor.moveToFirst()) {
        action(cursor)
    } else {
        null
    }
    cursor.close()
    return result
}

inline fun <reified T> SQLiteDatabase.selectMultipleAsList(
    tableName: String,
    columns: Array<String>,
    selection: String? = null,
    action: Cursor.() -> T
): List<T> {
    val cursor = query(
        tableName,
        columns,
        selection,
        null,
        null,
        null,
        null,
        null
    )
    val results = Array(cursor.count) {
        cursor.moveToPosition(it)
        action(cursor)
    }
    cursor.close()
    return results.toList()
}

inline fun SQLiteDatabase.selectMultiple(
    tableName: String,
    columns: Array<String>,
    selection: String? = null,
    action: Cursor.() -> Unit
) {
    val cursor = query(
        tableName,
        columns,
        selection,
        null,
        null,
        null,
        null,
        null
    )
    while (cursor.moveToNext()) {
        action(cursor)
    }
    cursor.close()
}