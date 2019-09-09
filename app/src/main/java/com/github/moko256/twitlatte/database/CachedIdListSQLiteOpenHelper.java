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

package com.github.moko256.twitlatte.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.github.moko256.latte.client.base.entity.AccessToken;

import java.io.File;
import java.util.List;

import kotlin.collections.ArraysKt;

/**
 * Created by moko256 on 2017/06/08.
 *
 * @author moko256
 */

public class CachedIdListSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String ID_LIST_TABLE_NAME = "IdList";
    private static final String[] COLUMNS = new String[]{"id"};

    private static final String SEEING_ID_TABLE_NAME = "SeeingId";

    private static final String insertIdListStatement = "insert into " + ID_LIST_TABLE_NAME + " values(?)";
    private static final String deleteIdListStatement = "delete from " + ID_LIST_TABLE_NAME + " where id=?";

    public CachedIdListSQLiteOpenHelper(Context context, AccessToken accessToken, String name) {
        super(context, accessToken != null ? new File(context.getCacheDir(), accessToken.getKeyString() + "/" + name + ".db").getAbsolutePath() : null, null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + ID_LIST_TABLE_NAME + "(id)");
        db.execSQL("create table " + SEEING_ID_TABLE_NAME + "(id)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("drop table IdList");
            db.execSQL("drop table ListViewPosition");
            onCreate(db);
        }
        // else (oldVersion < 3) ...
    }

    public List<Long> getIds() {
        final long[] ids;

        final Cursor c = getReadableDatabase().query(ID_LIST_TABLE_NAME, COLUMNS, null, null, null, null, null);
        final int count = c.getCount();
        ids = new long[count];

        int i = 1;
        while (c.moveToNext()) {
            ids[count - i] = c.getLong(0);
            i++;
        }

        c.close();

        return ArraysKt.asList(ids);
    }

    public void addIds(List<Long> ids) {
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        SQLiteStatement insert = database.compileStatement(insertIdListStatement);
        try {
            addIdsInner(insert, ids);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
        insert.close();
    }

    private void addIdsInner(SQLiteStatement statement, List<Long> ids) {
        for (int i = ids.size() - 1; i >= 0; i--) {
            statement.bindLong(1, ids.get(i));
            statement.execute();
        }
    }

    public void insertIds(int bottomPosition, List<Long> ids) {
        List<Long> n = getIds();
        List<Long> d = n.subList(0, bottomPosition);

        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        SQLiteStatement insert = database.compileStatement(insertIdListStatement);
        SQLiteStatement delete = database.compileStatement(deleteIdListStatement);
        try {
            deleteIdsInner(delete, d);
            addIdsInner(insert, ids);
            addIdsInner(insert, d);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
        delete.close();
        insert.close();
    }

    public void deleteIds(List<Long> ids) {
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        SQLiteStatement delete = database.compileStatement(deleteIdListStatement);
        try {
            deleteIdsInner(delete, ids);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
        delete.close();
    }

    private void deleteIdsInner(SQLiteStatement statement, List<Long> ids) {
        for (Long id : ids) {
            statement.bindLong(1, id);
            statement.execute();
        }
    }

    public long getSeeingId() {
        long r;

        Cursor c = getReadableDatabase()
                .query(SEEING_ID_TABLE_NAME, COLUMNS, null, null, null, null, null);
        if (c.moveToNext()) {
            r = c.getLong(0);
        } else {
            r = 0L;
        }
        c.close();

        return r;
    }

    public void setSeeingId(Long i) {
        ContentValues contentValues = new ContentValues(1);
        contentValues.put("id", i);

        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        try {
            database.delete(SEEING_ID_TABLE_NAME, null, null);
            database.insert(SEEING_ID_TABLE_NAME, null, contentValues);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

}
