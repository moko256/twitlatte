/*
 * Copyright 2016 The twicalico authors
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

package com.github.moko256.twicalico.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.github.moko256.twicalico.BuildConfig;

import java.io.File;

/**
 * Created by moko256 on 2017/09/20.
 *
 * @author moko256
 */

class CachedIdListListSQLiteOpenHelper extends SQLiteOpenHelper {

    CachedIdListListSQLiteOpenHelper(Context context, long userId){
        super(context, new File(context.getCacheDir(), String.valueOf(userId) + "/IdListList.db").getAbsolutePath(), null, BuildConfig.CACHE_DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table IdListList(tableName);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void addTable(String tableName){
        ContentValues contentValues = new ContentValues(1);
        contentValues.put("tableName", tableName);
        SQLiteDatabase database = getWritableDatabase();
        database.insert("IdListList", null, contentValues);
        database.close();
    }

}
