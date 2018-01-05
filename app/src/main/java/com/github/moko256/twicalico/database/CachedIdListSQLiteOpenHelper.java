/*
 * Copyright 2017 The twicalico authors
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
import android.database.sqlite.SQLiteOpenHelper;

import com.github.moko256.twicalico.BuildConfig;

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

    private CachedStatusesCountSQLiteOpenHelper statusesCounts;
    private String databaseName;

    public CachedIdListSQLiteOpenHelper(Context context, long userId, String name){
        super(context, new File(context.getCacheDir(), String.valueOf(userId) + "/" + name + ".db").getAbsolutePath(), null, BuildConfig.CACHE_DATABASE_VERSION);
        databaseName = name;
        statusesCounts = new CachedStatusesCountSQLiteOpenHelper(context, userId);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + databaseName + "(id);");
        db.execSQL("create table ListViewPosition(position);");
        //db.execSQL("create table ListViewPositionOffset(offset);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*
        switch (oldVersion){
            case 1:
                db.execSQL("create table ListViewPositionOffset(offset);");
                break;
        }
        */
    }

    public synchronized ArrayList<Long> getIds(){
        SQLiteDatabase database=getReadableDatabase();
        Cursor c=database.query(databaseName, new String[]{"id"}, null, null, null,null,null);
        ArrayList<Long> ids = new ArrayList<>(c.getCount());

        while (c.moveToNext()){
            ids.add(c.getLong(0));
        }

        c.close();
        database.close();

        Collections.reverse(ids);
        return ids;
    }

    public synchronized void addIds(List<Long> ids){
        SQLiteDatabase database=getWritableDatabase();
        database.beginTransaction();
        addIdsAtTransaction(ids);
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
    }

    private void addIdsAtTransaction(List<Long> ids){
        SQLiteDatabase database=getWritableDatabase();
        List<Long> idsList = new ArrayList<>();

        for (int i = ids.size() - 1; i >= 0; i--) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("id", ids.get(i));

            database.insert(databaseName, "", contentValues);
            if (ids.get(i) != -1L){
                idsList.add(ids.get(i));
            }
        }
        statusesCounts.incrementCounts(idsList);
    }

    private void addIdsOnlyAtTransaction(List<Long> ids){
        SQLiteDatabase database=getWritableDatabase();

        for (int i = ids.size() - 1; i >= 0; i--) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("id", ids.get(i));

            database.insert(databaseName, "", contentValues);
        }
    }

    public synchronized void insertIds(int bottomPosition, List<Long> ids){
        ArrayList<Long> n = getIds();
        List<Long> d = n.subList(0, bottomPosition);

        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        deleteOnlyIdsAtTransaction(d);
        addIdsAtTransaction(ids);
        addIdsOnlyAtTransaction(d);
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
    }

    public synchronized void deleteIds(List<Long> ids){
        SQLiteDatabase database=getWritableDatabase();
        database.beginTransaction();
        deleteIdsAtTransaction(ids);
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
    }

    private void deleteIdsAtTransaction(List<Long> ids){
        SQLiteDatabase database = getWritableDatabase();
        List<Long> idsList = new ArrayList<>();
        for (long id : ids) {
            database.delete(databaseName, "id=" + String.valueOf(id), null);

            if (id != -1L){
                idsList.add(id);
            }
        }
        statusesCounts.decrementCounts(idsList);
    }

    private void deleteOnlyIdsAtTransaction(List<Long> ids){
        SQLiteDatabase database = getWritableDatabase();
        for (long id : ids) {
            database.delete(databaseName, "id=" + String.valueOf(id), null);
        }
    }

    public boolean[] hasIdsOtherTable(List<Long> ids){
        boolean[] result = new boolean[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            result[i] = !(ids.get(0) != -1 && statusesCounts.getCount(ids.get(i)) == 0);
        }
        return result;
    }

    public synchronized int getListViewPosition(){
        SQLiteDatabase database=getReadableDatabase();
        Cursor c = database.query("ListViewPosition", new String[]{"position"}, null, null, null, null, null);
        int r;
        if (c.moveToNext()){
            r = c.getInt(0);
        } else {
            r = 0;
        }
        c.close();
        database.close();
        return r;
    }

    public synchronized void setListViewPosition(int i){
        SQLiteDatabase database=getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("position", i);

        database.beginTransaction();
        database.delete("ListViewPosition", null, null);
        database.insert("ListViewPosition", null, contentValues);
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
    }

    /*

    public synchronized int getListViewPositionOffset(){
        SQLiteDatabase database=getReadableDatabase();
        Cursor c = database.query("ListViewPositionOffset", new String[]{"offset"}, null, null, null, null, null);
        int r;
        if (c.moveToNext()){
            r = c.getInt(0);
        } else {
            r = 0;
        }
        c.close();
        database.close();
        return r;
    }

    public synchronized void setListViewPositionOffset(int i){
        SQLiteDatabase database=getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("offset", i);

        database.beginTransaction();
        database.delete("ListViewPositionOffset", null, null);
        database.insert("ListViewPositionOffset", null, contentValues);
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
    }

    */
}
