/*
 * Copyright 2015-2018 The twitlatte authors
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

import com.github.moko256.core.client.base.entity.AccessToken;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by moko256 on 2017/06/08.
 *
 * @author moko256
 */

public class CachedIdListSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String ID_LIST_TABLE_NAME = "IdList";

    private static final String SEEING_ID_TABLE_NAME = "SeeingId";

    public CachedIdListSQLiteOpenHelper(Context context, AccessToken accessToken, String name){
        super(context, accessToken != null? new File(context.getCacheDir(), accessToken.getKeyString() + "/" + name + ".db").getAbsolutePath(): null, null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + ID_LIST_TABLE_NAME + "(id)");
        db.execSQL("create table " + SEEING_ID_TABLE_NAME + "(id)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1) {
            Cursor ids = db.query(ID_LIST_TABLE_NAME, new String[]{"id"}, null, null, null, null, null);
            Cursor positions = db.query("ListViewPosition", new String[]{"position"}, null, null, null, null, null);
            db.execSQL("create table " + SEEING_ID_TABLE_NAME + "(id)");
            if (positions.moveToNext()) {
                boolean hasId = ids.moveToPosition(positions.getInt(0));
                positions.close();
                if (hasId) {
                    int i = ids.getInt(0);
                    int count = ids.getColumnCount();

                    ContentValues contentValues = new ContentValues(1);
                    contentValues.put("id", count - i -1);
                    db.insert(SEEING_ID_TABLE_NAME, null, contentValues);
                }
                ids.close();
            }
            db.execSQL("drop table ListViewPosition");
        }

        // else (oldVersion <= 2) ...
    }

    public List<Long> getIds(){
        List<Long> ids;

        synchronized (this) {
            SQLiteDatabase database = getReadableDatabase();
            Cursor c = database.query(ID_LIST_TABLE_NAME, new String[]{"id"}, null, null, null, null, null);
            ids = new ArrayList<>(c.getCount());

            while (c.moveToNext()) {
                ids.add(c.getLong(0));
            }

            c.close();
            database.close();
        }

        Collections.reverse(ids);
        return ids;
    }

    public void addIds(List<Long> ids){
        synchronized (this) {
            SQLiteDatabase database = getWritableDatabase();
            database.beginTransaction();
            addIdsInner(ids);
            database.setTransactionSuccessful();
            database.endTransaction();
            database.close();
        }
    }

    private void addIdsInner(List<Long> ids){
        SQLiteDatabase database=getWritableDatabase();

        for (int i = ids.size() - 1; i >= 0; i--) {
            ContentValues contentValues = new ContentValues(1);
            contentValues.put("id", ids.get(i));

            database.insert(ID_LIST_TABLE_NAME, "", contentValues);
        }
    }

    public void insertIds(int bottomPosition, List<Long> ids){
        synchronized (this) {
            List<Long> n = getIds();
            List<Long> d = n.subList(0, bottomPosition);

            SQLiteDatabase database = getWritableDatabase();
            database.beginTransaction();
            deleteIdsInner(d);
            addIdsInner(ids);
            addIdsInner(d);
            database.setTransactionSuccessful();
            database.endTransaction();
            database.close();
        }
    }

    public void deleteIds(List<Long> ids){
        synchronized (this) {
            SQLiteDatabase database = getWritableDatabase();
            database.beginTransaction();
            deleteIdsInner(ids);
            database.setTransactionSuccessful();
            database.endTransaction();
            database.close();
        }
    }

    private void deleteIdsInner(List<Long> ids){
        SQLiteDatabase database = getWritableDatabase();
        for (Long id : ids) {
            database.delete(ID_LIST_TABLE_NAME, "id=" + String.valueOf(id), null);
        }
    }

    public long getSeeingId(){
        long r;

        synchronized (this) {
            SQLiteDatabase database = getReadableDatabase();
            Cursor c = database.query(SEEING_ID_TABLE_NAME, new String[]{"id"}, null, null, null, null, null);
            if (c.moveToNext()) {
                r = c.getLong(0);
            } else {
                r = 0L;
            }
            c.close();
            database.close();
        }

        return r;
    }

    public void setSeeingId(long i){
        synchronized (this) {
            SQLiteDatabase database = getWritableDatabase();

            ContentValues contentValues = new ContentValues(1);
            contentValues.put("id", i);

            database.beginTransaction();
            database.delete(SEEING_ID_TABLE_NAME, null, null);
            database.insert(SEEING_ID_TABLE_NAME, null, contentValues);
            database.setTransactionSuccessful();
            database.endTransaction();
            database.close();
        }
    }

}
