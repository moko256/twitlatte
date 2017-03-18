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

package com.github.moko256.twicalico;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import twitter4j.Status;

/**
 * Created by moko256 on 2017/03/17.
 *
 * @author moko256
 */

public class CachedStatusesSQLiteOpenHelper extends SQLiteOpenHelper {

    public CachedStatusesSQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context, new File(context.getCacheDir(), name).getAbsolutePath(), factory, version);
    }
    public CachedStatusesSQLiteOpenHelper(Context context){
        super(context, "CachedStatuses.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table CachedStatuses(id string, status blob);"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public Status getCachedStatus(long id){
        Status status = null;
        SQLiteDatabase database=getReadableDatabase();
        Cursor c=database.query(
                "CachedStatuses",
                new String[]{"id", "status"},
                "id=?", new String[]{String.valueOf(id)}
                ,null,null,null
        );
        if (c.moveToLast()){
            try {
                ByteArrayInputStream byteArrayInputStream =new ByteArrayInputStream(c.getBlob(1));
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                status =(Status) objectInputStream.readObject();
                objectInputStream.close();
                byteArrayInputStream.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        c.close();
        database.close();
        return status;
    }

    public void addCachedStatus(Status status){
        byte[] serializedStatusByte = null;

        try {
            ByteArrayOutputStream byteArrayOutputStream;
            ObjectOutputStream objectOutputStream;
            byteArrayOutputStream = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(status);
            serializedStatusByte = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (serializedStatusByte != null){
            SQLiteDatabase database=getWritableDatabase();

            ContentValues contentValues = new ContentValues();
            contentValues.put("id", String.valueOf(status.getId()));
            contentValues.put("status", serializedStatusByte);

            Cursor c=database.query(
                    "CachedStatuses",
                    new String[]{"id", "status"},
                    "id=?", new String[]{String.valueOf(status.getId())}
                    ,null,null,null
            );

            if (c.moveToNext()){
                database.update("CachedStatuses", contentValues, "id=?", new String[]{String.valueOf(status.getId())});
            } else {
                database.insert("CachedStatuses", "zero", contentValues);
            }

            c.close();
            database.close();
        }
    }

    public void deleteCachedStatus(long id){
        SQLiteDatabase database=getWritableDatabase();
        database.delete("CachedStatuses", "id=?", new String[]{String.valueOf(id)});
    }
}