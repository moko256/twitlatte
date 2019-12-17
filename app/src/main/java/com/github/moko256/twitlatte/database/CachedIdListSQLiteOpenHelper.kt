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

import android.content.Context
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteStatement
import com.github.moko256.latte.client.base.entity.AccessToken
import com.github.moko256.twitlatte.database.utils.read
import com.github.moko256.twitlatte.database.utils.transaction
import com.github.moko256.twitlatte.database.utils.write
import java.io.File

/**
 * Created by moko256 on 2019/12/07.
 *
 * @author moko256
 */
class CachedIdListSQLiteOpenHelper(
    context: Context,
    accessToken: AccessToken?,
    name: String

) : SQLiteOpenHelper(
    context,
    accessToken?.let {
        File(
            context.cacheDir,
            "${it.getKeyString()}/$name.db"
        ).absolutePath
    },
    null,
    2
) {
    companion object {
        private val COLUMNS = arrayOf("id")

        private const val ID_LIST_TABLE_NAME = "IdList"
        private const val SEEING_ID_TABLE_NAME = "SeeingId"

        private const val insertIdListStatement = "insert into $ID_LIST_TABLE_NAME values(?)"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("create table $ID_LIST_TABLE_NAME(id)")
        db.execSQL("create table $SEEING_ID_TABLE_NAME(id)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("drop table IdList")
            db.execSQL("drop table ListViewPosition")
            onCreate(db)
        }
        // else (oldVersion < 3) ...
    }

    fun getIds(): List<Long> {
        return read {
            getIdsInner(this)
        }
    }

    private fun getIdsInner(db: SQLiteDatabase): List<Long> {
        val c = db.query(ID_LIST_TABLE_NAME, COLUMNS, null, null, null, null, null)
        val count = c.count
        val ids = LongArray(count)

        var i = count
        while (c.moveToNext()) {
            i--
            ids[i] = c.getLong(0)
        }

        c.close()
        return ids.asList()
    }

    fun insertIdsAtFirst(ids: List<Long>) {
        transaction {
            val insert = compileStatement(insertIdListStatement)

            addIdsInner(insert, ids)

            insert.close()
        }
    }

    fun insertIdsAtLast(ids: List<Long>) {
        transaction {
            val n = getIdsInner(this)

            val insert = compileStatement(insertIdListStatement)

            deleteAllIds(this)
            addIdsInner(insert, ids)
            addIdsInner(insert, n)

            insert.close()
        }
    }

    fun insertIdsAt(index: Int, ids: List<Long>) {
        transaction {
            val n = getIdsInner(this)
            val d = n.subList(0, index)

            val insert = compileStatement(insertIdListStatement)

            deleteTopIds(this, n.size, index)
            addIdsInner(insert, ids)
            addIdsInner(insert, d)

            insert.close()
        }
    }

    fun removeAt(index: Int) {
        write {
            val offset = DatabaseUtils.queryNumEntries(this, ID_LIST_TABLE_NAME).toInt() - index - 1
            execSQL(
                "delete from $ID_LIST_TABLE_NAME where rowid in (select rowid from $ID_LIST_TABLE_NAME limit 1 offset $offset)"
            )
        }
    }

    fun removeFromLast(count: Int) {
        write {
            execSQL(
                "delete from $ID_LIST_TABLE_NAME where rowid in (select rowid from $ID_LIST_TABLE_NAME limit $count)"
            )
        }
    }

    private fun addIdsInner(statement: SQLiteStatement, ids: List<Long>) {
        for (i in ids.indices.reversed()) {
            statement.bindLong(1, ids[i])
            statement.execute()
        }
    }

    private fun deleteAllIds(database: SQLiteDatabase) {
        database.execSQL("delete from $ID_LIST_TABLE_NAME")
    }

    private fun deleteTopIds(database: SQLiteDatabase, listSize: Int, toIndex: Int) {
        val offset = listSize - toIndex
        database.execSQL(
            "delete from $ID_LIST_TABLE_NAME where rowid in (select rowid from $ID_LIST_TABLE_NAME limit $toIndex offset $offset)"
        )
    }


    fun getSeeingId(): Long {
        return read {
            val c = query(SEEING_ID_TABLE_NAME, COLUMNS, null, null, null, null, null)
            val r = if (c.moveToNext()) {
                c.getLong(0)
            } else {
                0L
            }
            c.close()
            r
        }
    }

    fun setSeeingId(id: Long?) {
        transaction {
            delete(SEEING_ID_TABLE_NAME, null, null)
            execSQL("insert into $SEEING_ID_TABLE_NAME values($id)")
        }
    }

}