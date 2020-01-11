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

import android.content.Context
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDoneException
import android.os.Build
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by moko256 on 2019/07/12.
 *
 * @author moko256
 */

@RunWith(AndroidJUnit4::class)
class TableDBUtilKtCreateTableWithUniqueKeyTest {

    @Test
    fun createTableWithUniqueIntKey() {
        val testName = "createTableWithUniqueIntKey"

        val db = ApplicationProvider.getApplicationContext<Context>()
            .openOrCreateDatabase(testName, 0, null)

        db.createTableWithUniqueIntKey("test02", arrayOf("key", "c0", "c1"), 0)
        assertEquals(
            "CREATE TABLE test02(key integer primary key,c0,c1)",
            db.tableQuery("test02")
        )

        db.createTableWithUniqueIntKey("test03", arrayOf("c0", "key", "c1"), 1)
        assertEquals(
            "CREATE TABLE test03(c0,key integer primary key,c1)",
            db.tableQuery("test03")
        )
    }

    @Test
    fun createTableWithUniqueKey() {
        val testName = "createTableWithUniqueKey"

        val db = ApplicationProvider.getApplicationContext<Context>()
            .openOrCreateDatabase(testName, 0, null)

        db.createTableWithUniqueKey("test01", arrayOf("key", "c0", "c1"), arrayOf("key"))

        val test01Query = db.tableQuery("test01")
        val test01Index = db.indexQuery("test01_index")

        if (Build.VERSION.SDK_INT >= 24) {
            Log.d(testName, "Enable \"without rowId\"")

            assertEquals(
                "CREATE TABLE test01(key,c0,c1,primary key(key)) without rowId",
                test01Query
            )
            assertNull(test01Index)
        } else {
            Log.d(testName, "Disable \"without rowId\"")

            assertEquals("CREATE TABLE test01(key,c0,c1,primary key(key))", test01Query)
            assertEquals("CREATE UNIQUE INDEX test01_index on test01(key)", test01Index)
        }
    }


    private fun SQLiteDatabase.tableQuery(tableName: String) = selectFromMaster("table", tableName)
    private fun SQLiteDatabase.indexQuery(indexName: String) = selectFromMaster("index", indexName)

    private fun SQLiteDatabase.selectFromMaster(type: String, name: String): String? {
        return try {
            DatabaseUtils.stringForQuery(
                this,
                //language=RoomSql
                "select sql from sqlite_master where type=? and name=?",
                arrayOf(type, name)
            )
        } catch (ignore: SQLiteDoneException) {
            null
        }
    }
}