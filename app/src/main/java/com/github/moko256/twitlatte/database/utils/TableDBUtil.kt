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

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase

/**
 * Created by moko256 on 2019/03/07.
 *
 * @author moko256
 */

fun SQLiteDatabase.createTableWithUniqueIntKey(
        tableName: String,
        columns: Array<String>,
        keyPosition: Int
) {
    val builder = StringBuilder(columns.size * 8 + 16)
            .append("create table ")
            .append(tableName)
            .append("(")

    columns.forEachIndexed { index, c ->
        builder.append(c)
        if (keyPosition == index) {
            builder.append(" integer primary key")
        }
        if (index < columns.size - 1) {
            builder.append(",")
        }
    }

    builder.append(")")
    execSQL(builder.toString())
}


fun SQLiteDatabase.createTableWithUniqueKey(
        tableName: String,
        columns: Array<String>,
        keys: Array<String>
) {
    val builder = StringBuilder(columns.size * 8 + 28)
            .append("create table ")
            .append(tableName)
            .append("(")

    columns.joinTo(builder, ",")

    builder.append(",primary key(")
    keys.joinTo(builder, ",")
    builder.append("))")
    try {
        builder.append(" without rowId")
        execSQL(builder.toString())
    } catch (ignore: SQLException) {
        builder.delete(builder.length - 14, builder.length)
        execSQL(builder.toString())
        execSQL("create unique index ${tableName}_index on $tableName(${keys.joinToString(",")})")
    }
}

fun SQLiteDatabase.addColumn(tableName: String, columnName: String, defaultValue: String? = null) {
    execSQL("alter table $tableName add column $columnName")
    defaultValue?.let {
        execSQL("update $tableName set $columnName=$defaultValue")
    }
}
