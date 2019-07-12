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

import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by moko256 on 2019/07/12.
 *
 * @author moko256
 */

private fun SQLiteDatabase.getTableQuery(tableName: String): String {
    val query = rawQuery("select sql from sqlite_master where type='table' and name='$tableName'", null)
    query.moveToLast()
    val result = query.getString(0)
    query.close()
    return result
}

@RunWith(AndroidJUnit4::class)
class DBUtilKtCreateTableWithUniqueKeyTest {

    @Test
    fun createTableWithUniqueKey() {
        val helper = EmptyDBHelper(ApplicationProvider.getApplicationContext(), "createTableWithUniqueKey")

        val db = helper.writableDatabase
        db.createTableWithUniqueKey("test01", arrayOf("key", "c0", "c1"), 0)
        val test01Query = db.getTableQuery("test01")
        if (Build.VERSION.SDK_INT >= 24) {
            Log.d("createTableWithUniqueKey", "Enable \"without rowId\"")
            assertEquals("CREATE TABLE test01(key primary key,c0,c1) without rowId", test01Query)
        } else {
            Log.d("createTableWithUniqueKey", "Disable \"without rowId\"")
            assertEquals("CREATE TABLE test01(key primary key,c0,c1)", test01Query)
        }
        db.execSQL("delete from test01")

        db.createTableWithUniqueKey("test02", arrayOf("key", "c0", "c1"), 0, true)
        assertEquals("CREATE TABLE test02(key integer primary key,c0,c1)", db.getTableQuery("test02"))
        db.execSQL("delete from test02")

        db.createTableWithUniqueKey("test03", arrayOf("c0", "key", "c1"), 1, true)
        assertEquals("CREATE TABLE test03(c0,key integer primary key,c1)", db.getTableQuery("test03"))
        db.execSQL("delete from test03")
    }
}