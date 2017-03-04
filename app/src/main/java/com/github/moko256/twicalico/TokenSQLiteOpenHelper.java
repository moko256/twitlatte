package com.github.moko256.twicalico;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import twitter4j.auth.AccessToken;

/**
 * Created by moko256 on 2016/07/31.
 *
 * @author moko256
 */
public class TokenSQLiteOpenHelper extends SQLiteOpenHelper {
    public TokenSQLiteOpenHelper(Context context){
        super(context,"AccountTokenList.db",null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                "create table AccountTokenList(userName string ,userId string , token string , tokenSecret string);"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public AccessToken getAccessToken(int index){
        SQLiteDatabase database=getReadableDatabase();
        Cursor c=database.query("AccountTokenList",new String[]{"userName","userId","token","tokenSecret"},null,null,null,null,null);
        if (!c.moveToPosition(index)){
            return null;
        }

        AccessToken accessToken=new AccessToken(c.getString(2),c.getString(3)){
            String screenName = c.getString(0);
            long userId = Long.parseLong(c.getString(1),10);

            @Override
            public String getScreenName() {
                return screenName;
            }

            @Override
            public long getUserId() {
                return userId;
            }
        };

        c.close();
        database.close();

        return accessToken;
    }

    public long addAccessToken(AccessToken accessToken){
        SQLiteDatabase database=getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("userName",accessToken.getScreenName());
        contentValues.put("userId",String.valueOf(accessToken.getUserId()));
        contentValues.put("token",accessToken.getToken());
        contentValues.put("tokenSecret",accessToken.getTokenSecret());

        Cursor c=database.query(
                "AccountTokenList",
                new String[]{"userName","userId","token","tokenSecret"},
                "userId=?", new String[]{String.valueOf(accessToken.getUserId())}
                ,null,null,null
        );

        if (c.moveToNext()){
            database.update("AccountTokenList",contentValues,"userId=?", new String[]{String.valueOf(accessToken.getUserId())});
        } else {
            database.insert("AccountTokenList", "zero", contentValues);
        }

        c.close();

        long count = DatabaseUtils.queryNumEntries(database,"AccountTokenList");
        database.close();
        return count;
    }

    public long deleteAccessToken(long userId){
        SQLiteDatabase database=getWritableDatabase();
        database.delete("AccountTokenList","userId=?",new String[]{String.valueOf(userId)});
        long count = DatabaseUtils.queryNumEntries(database,"AccountTokenList");
        database.close();
        return count;
    }


}
