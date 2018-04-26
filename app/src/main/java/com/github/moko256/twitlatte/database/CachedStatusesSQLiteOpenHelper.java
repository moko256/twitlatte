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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.moko256.mastodon.MTStatus;
import com.github.moko256.twitlatte.BuildConfig;
import com.github.moko256.twitlatte.array.ArrayUtils;
import com.github.moko256.twitlatte.cacheMap.StatusCacheMap;
import com.github.moko256.twitlatte.entity.Emoji;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.SymbolEntity;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

/**
 * Created by moko256 on 2017/03/17.
 *
 * @author moko256
 */

public class CachedStatusesSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String TABLE_NAME = "CachedStatuses";
    private static final String[] TABLE_COLUMNS = new String[]{
            "createdAt",
            "id",
            "userId",
            "retweetedStatusId",
            "text",
            "source",
            "inReplyToStatusId",
            "inReplyToUserId",
            "isFavorited",
            "isRetweeted",
            "favoriteCount",
            "inReplyToScreenName",
            "retweetCount",
            "isPossiblySensitive",
            "lang",
            "UserMentionEntity_texts",
            "UserMentionEntity_ids",
            "UserMentionEntity_names",
            "UserMentionEntity_screenNames",
            "UserMentionEntity_starts",
            "UserMentionEntity_ends",
            "URLEntity_texts",
            "URLEntity_expandedURLs",
            "URLEntity_displayURLs",
            "URLEntity_starts",
            "URLEntity_ends",
            "HashtagEntity_texts",
            "HashtagEntity_starts",
            "HashtagEntity_ends",
            "MediaEntity_texts",
            "MediaEntity_ids",
            "MediaEntity_MediaURLs",
            "MediaEntity_MediaURLHttpSs",
            "MediaEntity_types",
            "MediaEntity_Variants_bitrates",
            "MediaEntity_Variants_contentTypes",
            "MediaEntity_Variants_uris",
            "MediaEntity_starts",
            "MediaEntity_ends",
            "SymbolEntity_texts",
            "SymbolEntity_starts",
            "SymbolEntity_ends",
            "quotedStatusId",
            "url",
            "Emoji_shortcodes",
            "Emoji_urls",
            "count"
    };

    public CachedStatusesSQLiteOpenHelper(Context context, long userId){
        super(context, new File(context.getCacheDir(), String.valueOf(userId) + "/CachedStatuses.db").getAbsolutePath(), null, BuildConfig.CACHE_DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " + TABLE_NAME + "(" + ArrayUtils.toCommaSplitString(TABLE_COLUMNS) + ", primary key(id))"
        );
        db.execSQL("create unique index idindex on " + TABLE_NAME + "(id)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void incrementCounts(List<Long> ids){
        int[] counts = new int[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            counts[i] = getCount(ids.get(i));
        }

        SQLiteDatabase database=getWritableDatabase();
        database.beginTransaction();
        for (int i = 0; i < ids.size(); i++) {
            incrementCountAtTransaction(ids.get(i), counts[i]);
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
    }

    public void decrementCounts(List<Long> ids){
        int[] counts = new int[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            counts[i] = getCount(ids.get(i));
        }

        SQLiteDatabase database=getWritableDatabase();
        database.beginTransaction();
        for (int i = 0; i < ids.size(); i++) {
            decrementCountAtTransaction(ids.get(i), counts[i]);
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
    }

    private void incrementCountAtTransaction(long id, int count){
        ContentValues contentValues = new ContentValues(1);
        contentValues.put("id", id);
        contentValues.put("count", count + 1);
        SQLiteDatabase database = getWritableDatabase();
        database.replace(TABLE_NAME, null, contentValues);
    }

    private void decrementCountAtTransaction(long id, int oldCount) {
        SQLiteDatabase database = getWritableDatabase();
        int count = oldCount - 1;
        if (count == 0) {
            database.delete(TABLE_NAME, "id=" + String.valueOf(id), null);
        } else {
            ContentValues contentValues = new ContentValues(1);
            contentValues.put("id", id);
            contentValues.put("count", count);
            database.replace(TABLE_NAME, null, contentValues);
        }
    }

    public int getCount(long id){
        int count = 0;
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query(
                TABLE_NAME,
                new String[]{"id", "count"},
                "id=" + String.valueOf(id), null,
                null, null, null, "1"
        );
        if (cursor.moveToNext()){
            count = cursor.getInt(1);
        }
        cursor.close();
        database.close();
        return count;
    }

    public Status getCachedStatus(long id){
        Status status = null;
        SQLiteDatabase database=getReadableDatabase();
        Cursor c=database.query(
                TABLE_NAME,
                TABLE_COLUMNS,
                "id=" + String.valueOf(id), null
                ,null,null,null,"1"
        );
        if (c.moveToLast()){
            status = new StatusCacheMap.CachedStatus(
                    new Date(c.getLong(0)),
                    c.getLong(1),
                    c.getLong(2),
                    c.getLong(3),
                    c.getString(4),
                    c.getString(5),
                    c.getLong(6),
                    c.getLong(7),
                    c.getInt(8) != 0,
                    c.getInt(9) != 0,
                    c.getInt(10),
                    c.getString(11),
                    c.getInt(12),
                    c.getInt(13) != 0,
                    c.getString(14),
                    restoreUserMentionEntities(
                            spliteComma(c.getString(15)),
                            spliteComma(c.getString(16)),
                            spliteComma(c.getString(17)),
                            spliteComma(c.getString(18)),
                            spliteComma(c.getString(19)),
                            spliteComma(c.getString(20))
                    ),
                    restoreURLEntities(
                            spliteComma(c.getString(21)),
                            spliteComma(c.getString(22)),
                            spliteComma(c.getString(23)),
                            spliteComma(c.getString(24)),
                            spliteComma(c.getString(25))
                    ),
                    restoreHashtagEntities(
                            spliteComma(c.getString(26)),
                            spliteComma(c.getString(27)),
                            spliteComma(c.getString(28))
                    ),
                    restoreMediaEntities(
                            spliteComma(c.getString(29)),
                            spliteComma(c.getString(30)),
                            spliteComma(c.getString(31)),
                            spliteComma(c.getString(32)),
                            spliteComma(c.getString(33)),

                            parse(c.getString(34)),
                            parse(c.getString(35)),
                            parse(c.getString(36)),

                            spliteComma(c.getString(37)),
                            spliteComma(c.getString(38))
                    ),
                    restoreSymbolEntities(
                            spliteComma(c.getString(39)),
                            spliteComma(c.getString(40)),
                            spliteComma(c.getString(41))
                    ),
                    c.getLong(42),
                    c.getString(43),
                    restoreEmojis(
                            spliteComma(c.getString(44)),
                            spliteComma(c.getString(45))
                    )
            );
        }

        c.close();
        database.close();
        return status;
    }

    public void addCachedStatus(Status status, boolean incrementCount){
        ContentValues values = createCachedStatusContentValues(status);

        SQLiteDatabase database=getWritableDatabase();
        database.beginTransaction();
        database.replace(TABLE_NAME, null, values);
        if (incrementCount){
            database.execSQL("UPDATE " + TABLE_NAME + " SET count = count + 1 WHERE id=" + String.valueOf(status.getId()));
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
    }

    public void addCachedStatuses(Collection<? extends Status> statuses, boolean incrementCount){
        ArrayList<ContentValues> contentValues = new ArrayList<>(statuses.size());
        for (Status status : statuses) {
            contentValues.add(createCachedStatusContentValues(status));
        }

        SQLiteDatabase database=getWritableDatabase();
        database.beginTransaction();
        for (ContentValues values : contentValues) {
            database.replace(TABLE_NAME, null, values);
            if (incrementCount) {
                database.execSQL("UPDATE " + TABLE_NAME + " SET count = count + 1 WHERE id=" + String.valueOf(values.get(TABLE_COLUMNS[1])));
            }
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
    }

    private ContentValues createCachedStatusContentValues(Status status){
        ContentValues contentValues = new ContentValues();
        contentValues.put(TABLE_COLUMNS[0], status.getCreatedAt().getTime());
        contentValues.put(TABLE_COLUMNS[1], status.getId());
        contentValues.put(TABLE_COLUMNS[2], status.getUser().getId());
        contentValues.put(TABLE_COLUMNS[3], status.getRetweetedStatus() != null? status.getRetweetedStatus().getId(): -1);
        contentValues.put(TABLE_COLUMNS[4], status.getText());
        contentValues.put(TABLE_COLUMNS[5], status.getSource());
        contentValues.put(TABLE_COLUMNS[6], status.getInReplyToStatusId());
        contentValues.put(TABLE_COLUMNS[7], status.getInReplyToUserId());
        contentValues.put(TABLE_COLUMNS[8], status.isFavorited());
        contentValues.put(TABLE_COLUMNS[9], status.isRetweeted());
        contentValues.put(TABLE_COLUMNS[10], status.getFavoriteCount());
        contentValues.put(TABLE_COLUMNS[11], status.getInReplyToScreenName());
        contentValues.put(TABLE_COLUMNS[12], status.getRetweetCount());
        contentValues.put(TABLE_COLUMNS[13], status.isPossiblySensitive());
        contentValues.put(TABLE_COLUMNS[14], status.getLang());

        if (status.getUserMentionEntities() != null) {
            int size = status.getUserMentionEntities().length;
            String[] texts = new String[size];
            String[] ids = new String[size];
            String[] names = new String[size];
            String[] screenNames = new String[size];
            String[] starts = new String[size];
            String[] ends = new String[size];

            for(int i = 0;i < size; i++){
                UserMentionEntity entity = status.getUserMentionEntities()[i];
                texts[i] = entity.getText();
                ids[i] = String.valueOf(entity.getId());
                names[i] = entity.getName();
                screenNames[i] = entity.getScreenName();
                starts[i] = String.valueOf(entity.getStart());
                ends[i] = String.valueOf(entity.getEnd());
            }
            contentValues.put(TABLE_COLUMNS[15], ArrayUtils.toCommaSplitString(texts).toString());
            contentValues.put(TABLE_COLUMNS[16], ArrayUtils.toCommaSplitString(ids).toString());
            contentValues.put(TABLE_COLUMNS[17], ArrayUtils.toCommaSplitString(names).toString());
            contentValues.put(TABLE_COLUMNS[18], ArrayUtils.toCommaSplitString(screenNames).toString());
            contentValues.put(TABLE_COLUMNS[19], ArrayUtils.toCommaSplitString(starts).toString());
            contentValues.put(TABLE_COLUMNS[20], ArrayUtils.toCommaSplitString(ends).toString());
        }

        if (status.getURLEntities() != null) {
            int size = status.getURLEntities().length;
            String[] texts = new String[size];
            String[] expandedUrls = new String[size];
            String[] displayUrls = new String[size];
            String[] starts = new String[size];
            String[] ends = new String[size];

            for(int i = 0;i < size; i++){
                URLEntity entity = status.getURLEntities()[i];
                texts[i] = entity.getText();
                expandedUrls[i] = entity.getExpandedURL();
                displayUrls[i] = entity.getDisplayURL();
                starts[i] = String.valueOf(entity.getStart());
                ends[i] = String.valueOf(entity.getEnd());
            }
            contentValues.put(TABLE_COLUMNS[21], ArrayUtils.toCommaSplitString(texts).toString());
            contentValues.put(TABLE_COLUMNS[22], ArrayUtils.toCommaSplitString(expandedUrls).toString());
            contentValues.put(TABLE_COLUMNS[23], ArrayUtils.toCommaSplitString(displayUrls).toString());
            contentValues.put(TABLE_COLUMNS[24], ArrayUtils.toCommaSplitString(starts).toString());
            contentValues.put(TABLE_COLUMNS[25], ArrayUtils.toCommaSplitString(ends).toString());
        }

        if (status.getHashtagEntities() != null) {
            int size = status.getHashtagEntities().length;
            String[] texts = new String[size];
            String[] starts = new String[size];
            String[] ends = new String[size];

            for(int i = 0;i < size; i++){
                HashtagEntity entity = status.getHashtagEntities()[i];
                texts[i] = entity.getText();
                starts[i] = String.valueOf(entity.getStart());
                ends[i] = String.valueOf(entity.getEnd());
            }
            contentValues.put(TABLE_COLUMNS[26], ArrayUtils.toCommaSplitString(texts).toString());
            contentValues.put(TABLE_COLUMNS[27], ArrayUtils.toCommaSplitString(starts).toString());
            contentValues.put(TABLE_COLUMNS[28], ArrayUtils.toCommaSplitString(ends).toString());
        }

        if (status.getMediaEntities() != null) {
            int size = status.getMediaEntities().length;


            String[] texts = new String[size];
            String[] ids = new String[size];
            String[] mediaURLs = new String[size];
            String[] mediaURLHttpSs = new String[size];
            String[] types = new String[size];
            String[][] variants_bitrates = new String[size][];
            String[][] variants_contentTypes = new String[size][];
            String[][] variants_uris = new String[size][];
            String[] starts = new String[size];
            String[] ends = new String[size];

            for(int i = 0;i < size; i++){
                MediaEntity entity = status.getMediaEntities()[i];
                texts[i] = entity.getText();
                ids[i] = String.valueOf(entity.getId());
                mediaURLs[i] = entity.getMediaURL();
                mediaURLHttpSs[i] = entity.getMediaURLHttps();
                types[i] = entity.getMediaURL();
                if (entity.getVideoVariants() != null) {
                    for (int i1 = 0; i1 < entity.getVideoVariants().length; i1++) {
                        variants_bitrates[i][i1] = String.valueOf(entity.getVideoVariants()[i1].getBitrate());
                        variants_contentTypes[i][i1] = entity.getMediaURL();
                        variants_uris[i][i1] = entity.getMediaURLHttps();
                    }
                }
                starts[i] = String.valueOf(entity.getStart());
                ends[i] = String.valueOf(entity.getEnd());
            }
            contentValues.put(TABLE_COLUMNS[29], ArrayUtils.toCommaSplitString(texts).toString());
            contentValues.put(TABLE_COLUMNS[30], ArrayUtils.toCommaSplitString(ids).toString());
            contentValues.put(TABLE_COLUMNS[31], ArrayUtils.toCommaSplitString(mediaURLs).toString());
            contentValues.put(TABLE_COLUMNS[32], ArrayUtils.toCommaSplitString(mediaURLHttpSs).toString());
            contentValues.put(TABLE_COLUMNS[33], ArrayUtils.toCommaSplitString(types).toString());
            contentValues.put(TABLE_COLUMNS[34], ArrayUtils.toCommaAndPipeSplitString(variants_bitrates).toString());
            contentValues.put(TABLE_COLUMNS[35], ArrayUtils.toCommaAndPipeSplitString(variants_contentTypes).toString());
            contentValues.put(TABLE_COLUMNS[36], ArrayUtils.toCommaAndPipeSplitString(variants_uris).toString());
            contentValues.put(TABLE_COLUMNS[37], ArrayUtils.toCommaSplitString(starts).toString());
            contentValues.put(TABLE_COLUMNS[38], ArrayUtils.toCommaSplitString(ends).toString());
        }

        if (status.getSymbolEntities() != null) {
            int size = status.getSymbolEntities().length;
            String[] texts = new String[size];
            String[] starts = new String[size];
            String[] ends = new String[size];

            for(int i = 0;i < size; i++){
                SymbolEntity entity = status.getSymbolEntities()[i];
                texts[i] = entity.getText();
                starts[i] = String.valueOf(entity.getStart());
                ends[i] = String.valueOf(entity.getEnd());
            }
            contentValues.put(TABLE_COLUMNS[39], ArrayUtils.toCommaSplitString(texts).toString());
            contentValues.put(TABLE_COLUMNS[40], ArrayUtils.toCommaSplitString(starts).toString());
            contentValues.put(TABLE_COLUMNS[41], ArrayUtils.toCommaSplitString(ends).toString());
        }

        contentValues.put(TABLE_COLUMNS[42], status.getQuotedStatusId());

        String url;
        List<Emoji> emojis;

        if (status instanceof MTStatus) {
            url = ((MTStatus) status).status.getUrl();
            List<com.sys1yagi.mastodon4j.api.entity.Emoji> oldEmojis = ((MTStatus) status).status.getEmojis();

            emojis = new ArrayList<>(oldEmojis.size());
            for (com.sys1yagi.mastodon4j.api.entity.Emoji emoji : oldEmojis) {
                emojis.add(new Emoji(emoji.getShortcode(), emoji.getUrl()));
            }
        } else {
            url = "https://twitter.com/"
                    + status.getUser().getScreenName()
                    + "/status/"
                    + String.valueOf(status.getId());
            emojis = null;
        }

        contentValues.put(TABLE_COLUMNS[43], url);

        if (emojis != null){
            int size = emojis.size();
            String[] shortcodes = new String[size];
            String[] urls = new String[size];

            for(int i = 0;i < size; i++){
                Emoji emoji = emojis.get(i);
                shortcodes[i] = emoji.getShortCode();
                urls[i] = emoji.getUrl();
            }
            contentValues.put(TABLE_COLUMNS[44], ArrayUtils.toCommaSplitString(shortcodes).toString());
            contentValues.put(TABLE_COLUMNS[45], ArrayUtils.toCommaSplitString(urls).toString());
        }
        return contentValues;
    }

    public void deleteCachedStatus(long id){
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        deleteCachedStatusAtTransaction(database, id);
        database.delete(TABLE_NAME, "count=0", null);
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
    }

    public void deleteCachedStatuses(Collection<Long> ids){
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        for (Long id : ids) {
            deleteCachedStatusAtTransaction(database, id);
        }
        database.delete(TABLE_NAME, "count=0", null);
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
    }

    private void deleteCachedStatusAtTransaction(SQLiteDatabase database, Long id) {
        database.execSQL("UPDATE " + TABLE_NAME + " SET count = count - 1 WHERE id=" + String.valueOf(id));
    }

    @NonNull
    private String[] spliteComma(@Nullable String string){
        if (string != null) {
            return string.split(",");
        } else {
            return new String[0];
        }
    }

    private UserMentionEntity[] restoreUserMentionEntities(String[] texts,
                                                           String[] names,
                                                           String[] screenNames,
                                                           String[] ids,
                                                           String[] starts,
                                                           String[] ends){

        if (texts.length == 1 && texts[0].equals("")){
            return new UserMentionEntity[0];
        }

        UserMentionEntity[] entities = new UserMentionEntity[texts.length];
        for (int i = 0; i < entities.length; i++){
            int finalI = i;
            entities[i] = new UserMentionEntity() {
                @Override
                public String getText() {
                    return texts[finalI];
                }

                @Override
                public String getName() {
                    return names[finalI];
                }

                @Override
                public String getScreenName() {
                    return screenNames[finalI];
                }

                @Override
                public long getId() {
                    return Long.parseLong(ids[finalI]);
                }

                @Override
                public int getStart() {
                    return Integer.parseInt(starts[finalI]);
                }

                @Override
                public int getEnd() {
                    return Integer.parseInt(ends[finalI]);
                }
            };
        }
        return entities;
    }

    private URLEntity[] restoreURLEntities(String[] texts,
                                           String[] expandedURLs,
                                           String[] displaysURLs,
                                           String[] starts,
                                           String[] ends){

        if (texts.length == 1 && texts[0].equals("")){
            return new URLEntity[0];
        }

        URLEntity[] entities = new URLEntity[texts.length];
        for (int i = 0; i < entities.length; i++){
            int finalI = i;
            entities[i] = new URLEntity() {
                @Override
                public String getText() {
                    return texts[finalI];
                }

                @Override
                public String getURL() {
                    return texts[finalI];
                }

                @Override
                public String getExpandedURL() {
                    return expandedURLs[finalI];
                }

                @Override
                public String getDisplayURL() {
                    return displaysURLs[finalI];
                }

                @Override
                public int getStart() {
                    return Integer.valueOf(starts[finalI]);
                }

                @Override
                public int getEnd() {
                    return Integer.valueOf(ends[finalI]);
                }
            };
        }
        return entities;
    }

    private HashtagEntity[] restoreHashtagEntities(String[] texts,
                                               String[] starts,
                                               String[] ends){

        if (texts.length == 1 && texts[0].equals("")){
            return new HashtagEntity[0];
        }

        HashtagEntity[] entities = new HashtagEntity[texts.length];
        for (int i = 0; i < entities.length; i++){
            int finalI = i;
            entities[i] = new HashtagEntity() {
                @Override
                public String getText() {
                    return texts[finalI];
                }

                @Override
                public int getStart() {
                    return Integer.parseInt(starts[finalI]);
                }

                @Override
                public int getEnd() {
                    return Integer.parseInt(ends[finalI]);
                }
            };
        }
        return entities;
    }

    private MediaEntity[] restoreMediaEntities(String[] texts,
                                               String[] ids,
                                               String[] mediaUrls,
                                               String[] mediaUrlHttpSs,
                                               String[] types,
                                               String[][] variants_bitrates,
                                               String[][] variants_contentTypes,
                                               String[][] variants_urls,
                                               String[] starts,
                                               String[] ends){

        if (texts.length == 1 && texts[0].equals("")){
            return new MediaEntity[0];
        }

        MediaEntity[] entities = new MediaEntity[texts.length];
        for (int i = 0; i < entities.length; i++){
            int finalI = i;
            entities[i] = new MediaEntity() {
                @Override
                public long getId() {
                    return Long.parseLong(ids[finalI]);
                }

                @Override
                public String getMediaURL() {
                    return mediaUrls[finalI];
                }

                @Override
                public String getMediaURLHttps() {
                    return mediaUrlHttpSs[finalI];
                }

                @Override
                public Map<Integer, Size> getSizes() {
                    return null;
                }

                @Override
                public String getType() {
                    return types[finalI];
                }

                @Override
                public int getVideoAspectRatioWidth() {
                    return 0;
                }

                @Override
                public int getVideoAspectRatioHeight() {
                    return 0;
                }

                @Override
                public long getVideoDurationMillis() {
                    return 0;
                }

                @Override
                public Variant[] getVideoVariants() {
                    Variant[] result = new Variant[variants_urls.length];
                    for (int ii = 0; ii < variants_urls[ii].length; ii++){
                        int finalIi = ii;
                        result[ii] = new Variant() {
                            @Override
                            public int getBitrate() {
                                return Integer.parseInt(variants_bitrates[finalI][finalIi]);
                            }

                            @Override
                            public String getContentType() {
                                return variants_contentTypes[finalI][finalIi];
                            }

                            @Override
                            public String getUrl() {
                                return variants_urls[finalI][finalIi];
                            }
                        };
                    }
                    return result;
                }

                @Override
                public String getExtAltText() {
                    return null;
                }

                @Override
                public String getText() {
                    return texts[finalI];
                }

                @Override
                public String getURL() {
                    return texts[finalI];
                }

                @Override
                public String getExpandedURL() {
                    return null;
                }

                @Override
                public String getDisplayURL() {
                    return texts[finalI];
                }

                @Override
                public int getStart() {
                    return Integer.parseInt(starts[finalI]);
                }

                @Override
                public int getEnd() {
                    return Integer.parseInt(ends[finalI]);
                }
            };
        }
        return entities;
    }

    private String[][] parse(String string){
        if (string == null){
            return new String[0][0];
        }
        String[] resultA = string.split(",");
        String[][] result = new String[resultA.length][];
        for (int i = 0; i < resultA.length; i++) {
            result[i] = resultA[i].split("\\|");
        }
        return result;
    }

    private SymbolEntity[] restoreSymbolEntities(String[] texts,
                                                  String[] starts,
                                                  String[] ends){

        if (texts.length == 1 && texts[0].equals("")){
            return new SymbolEntity[0];
        }

        SymbolEntity[] entities = new SymbolEntity[texts.length];
        for (int i = 0; i < entities.length; i++){
            int finalI = i;
            entities[i] = new SymbolEntity() {
                @Override
                public String getText() {
                    return texts[finalI];
                }

                @Override
                public int getStart() {
                    return Integer.parseInt(starts[finalI]);
                }

                @Override
                public int getEnd() {
                    return Integer.parseInt(ends[finalI]);
                }
            };
        }
        return entities;
    }

    private List<Emoji> restoreEmojis(String[] shortcodes,
                                              String[] urls){

        List<Emoji> emojis = new ArrayList<>(shortcodes.length);
        for (int i = 0; i < shortcodes.length; i++) {
            emojis.add(new Emoji(shortcodes[i], urls[i]));
        }

        return emojis;
    }
}