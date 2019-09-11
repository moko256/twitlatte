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

import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.moko256.twitlatte.database.EmptyDBHelper
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
        val helper = EmptyDBHelper(ApplicationProvider.getApplicationContext(), "createTableWithUniqueIntKey")

        val db = helper.writableDatabase
        db.createTableWithUniqueIntKey("test02", arrayOf("key", "c0", "c1"), 0)
        assertEquals("CREATE TABLE test02(key integer primary key,c0,c1)", db.getTableQuery("test02"))

        db.createTableWithUniqueIntKey("test03", arrayOf("c0", "key", "c1"), 1)
        assertEquals("CREATE TABLE test03(c0,key integer primary key,c1)", db.getTableQuery("test03"))
    }

    @Test
    fun createTableWithUniqueKey() {
        val helper = EmptyDBHelper(ApplicationProvider.getApplicationContext(), "createTableWithUniqueKey")

        val db = helper.writableDatabase
        db.createTableWithUniqueKey("test01", arrayOf("key", "c0", "c1"), arrayOf("key"))

        val test01Query = db.getTableQuery("test01")
        val test01Index = db.getIndexQuery("test01_index")

        if (Build.VERSION.SDK_INT >= 24) {
            Log.d("createTableWithUniqueKey", "Enable \"without rowId\"")

            assertEquals("CREATE TABLE test01(key,c0,c1,primary key(key)) without rowId", test01Query)
            assertNull(test01Index)
        } else {
            Log.d("createTableWithUniqueKey", "Disable \"without rowId\"")

            assertEquals("CREATE TABLE test01(key,c0,c1,primary key(key))", test01Query)
            assertEquals("CREATE UNIQUE INDEX test01_index on test01(key)", test01Index)
        }
        db.execSQL("delete from test01")
    }

    private fun SQLiteDatabase.getTableQuery(tableName: String): String {
        val query = rawQuery("select sql from sqlite_master where type='table' and name='$tableName'", null)
        query.moveToLast()
        val result = query.getString(0)
        query.close()
        return result
    }

    private fun SQLiteDatabase.getIndexQuery(indexName: String): String? {
        val query = rawQuery("select sql from sqlite_master where type='index' and name='$indexName'", null)
        if (!query.moveToLast()) return null
        val result = query.getString(0)
        query.close()
        return result
    }
}