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
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.Nullable;
import android.text.TextUtils;

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

import kotlin.collections.ArraysKt;
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
            "MediaEntity_expandedURLs",
            "MediaEntity_displayURLs",
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

    public Status getCachedStatus(long id){
        Status status = null;
        SQLiteDatabase database = getReadableDatabase();
        Cursor c = database.query(
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
                            splitComma(c.getString(15)),
                            splitComma(c.getString(16)),
                            splitComma(c.getString(17)),
                            splitComma(c.getString(18)),
                            splitComma(c.getString(19)),
                            splitComma(c.getString(20))
                    ),
                    restoreURLEntities(
                            splitComma(c.getString(21)),
                            splitComma(c.getString(22)),
                            splitComma(c.getString(23)),
                            splitComma(c.getString(24)),
                            splitComma(c.getString(25))
                    ),
                    restoreHashtagEntities(
                            splitComma(c.getString(26)),
                            splitComma(c.getString(27)),
                            splitComma(c.getString(28))
                    ),
                    restoreMediaEntities(
                            splitComma(c.getString(29)),
                            splitComma(c.getString(30)),
                            splitComma(c.getString(31)),
                            splitComma(c.getString(32)),
                            splitComma(c.getString(33)),
                            splitComma(c.getString(34)),
                            splitComma(c.getString(35)),

                            parse(c.getString(36)),
                            parse(c.getString(37)),
                            parse(c.getString(38)),

                            splitComma(c.getString(39)),
                            splitComma(c.getString(40))
                    ),
                    restoreSymbolEntities(
                            splitComma(c.getString(41)),
                            splitComma(c.getString(42)),
                            splitComma(c.getString(43))
                    ),
                    c.getLong(44),
                    c.getString(45),
                    restoreEmojis(
                            splitComma(c.getString(46)),
                            splitComma(c.getString(47))
                    )
            );
        }

        c.close();
        database.close();
        return status;
    }

    public List<Long> getIdsInUse(List<Long> ids){
        ArrayList<Long> result = new ArrayList<>(ids.size() * 5);
        SQLiteDatabase database = getReadableDatabase();
        for (long id : ids) {
            Cursor c = database.query(
                    TABLE_NAME,
                    new String[]{TABLE_COLUMNS[1], TABLE_COLUMNS[3], TABLE_COLUMNS[44]},
                    "id=" + String.valueOf(id), null
                    , null, null, null
            );
            while(c.moveToNext()) {
                long repeatId = c.getLong(1);
                long quotedId = c.getLong(2);
                if (repeatId != -1){
                    if (!result.contains(repeatId) && !ids.contains(repeatId)) {
                        result.add(repeatId);
                    }
                } else if (quotedId != -1){
                    if (!result.contains(quotedId) && !ids.contains(quotedId)) {
                        result.add(quotedId);
                    }
                }
            }
            c.close();
        }
        database.close();
        if (result.size() > 0) {
            result.addAll(getIdsInUse(result));
        }
        return result;
    }

    public void addCachedStatus(StatusCacheMap.CachedStatus status, boolean incrementCount){
        ContentValues values = createCachedStatusContentValues(status);

        SQLiteDatabase database=getWritableDatabase();
        database.beginTransaction();
        database.replace(TABLE_NAME, null, values);
        if (incrementCount){
            SQLiteStatement statement = incrementCountStatement(database);
            statement.bindLong(1, status.getId());
            statement.execute();
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
    }

    public void addCachedStatuses(Collection<StatusCacheMap.CachedStatus> statuses, boolean incrementCount, long... excludeIncrementIds){
        ArrayList<ContentValues> contentValues = new ArrayList<>(statuses.size());
        for (StatusCacheMap.CachedStatus status : statuses) {
            contentValues.add(createCachedStatusContentValues(status));
        }

        SQLiteDatabase database=getWritableDatabase();
        database.beginTransaction();
        SQLiteStatement statement = incrementCount? incrementCountStatement(database): null;
        for (ContentValues values : contentValues) {
            database.replace(TABLE_NAME, null, values);

            Long id = values.getAsLong(TABLE_COLUMNS[1]);
            if (incrementCount && (excludeIncrementIds.length == 0 || !ArraysKt.contains(excludeIncrementIds, id))) {
                statement.bindLong(1, id);
                statement.execute();
            }
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
    }

    private SQLiteStatement incrementCountStatement(SQLiteDatabase database){
        return database.compileStatement("UPDATE " + TABLE_NAME + " SET count=count+1 WHERE id=?");
    }

    private ContentValues createCachedStatusContentValues(StatusCacheMap.CachedStatus status){
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
            String[] expandedUrls = new String[size];
            String[] displayUrls = new String[size];
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
                expandedUrls[i] = entity.getExpandedURL();
                displayUrls[i] = entity.getDisplayURL();
                ids[i] = String.valueOf(entity.getId());
                mediaURLs[i] = entity.getMediaURL();
                mediaURLHttpSs[i] = entity.getMediaURLHttps();
                types[i] = entity.getType();

                MediaEntity.Variant[] videoVariants = entity.getVideoVariants();
                if (videoVariants != null) {

                    int videosLength = videoVariants.length;

                    variants_bitrates[i] = new String[videosLength];
                    variants_contentTypes[i] = new String[videosLength];
                    variants_uris[i] = new String[videosLength];

                    for (int i1 = 0; i1 < videosLength; i1++) {
                        MediaEntity.Variant videoVariant = videoVariants[i1];
                        if (videoVariant != null) {
                            variants_bitrates[i][i1] = String.valueOf(videoVariant.getBitrate());
                            variants_contentTypes[i][i1] = videoVariant.getContentType();
                            variants_uris[i][i1] = videoVariant.getUrl();
                        }
                    }
                }
                starts[i] = String.valueOf(entity.getStart());
                ends[i] = String.valueOf(entity.getEnd());
            }
            contentValues.put(TABLE_COLUMNS[29], ArrayUtils.toCommaSplitString(texts).toString());
            contentValues.put(TABLE_COLUMNS[30], ArrayUtils.toCommaSplitString(expandedUrls).toString());
            contentValues.put(TABLE_COLUMNS[31], ArrayUtils.toCommaSplitString(displayUrls).toString());
            contentValues.put(TABLE_COLUMNS[32], ArrayUtils.toCommaSplitString(ids).toString());
            contentValues.put(TABLE_COLUMNS[33], ArrayUtils.toCommaSplitString(mediaURLs).toString());
            contentValues.put(TABLE_COLUMNS[34], ArrayUtils.toCommaSplitString(mediaURLHttpSs).toString());
            contentValues.put(TABLE_COLUMNS[35], ArrayUtils.toCommaSplitString(types).toString());
            contentValues.put(TABLE_COLUMNS[36], ArrayUtils.toCommaAndPipeSplitString(variants_bitrates).toString());
            contentValues.put(TABLE_COLUMNS[37], ArrayUtils.toCommaAndPipeSplitString(variants_contentTypes).toString());
            contentValues.put(TABLE_COLUMNS[38], ArrayUtils.toCommaAndPipeSplitString(variants_uris).toString());
            contentValues.put(TABLE_COLUMNS[39], ArrayUtils.toCommaSplitString(starts).toString());
            contentValues.put(TABLE_COLUMNS[40], ArrayUtils.toCommaSplitString(ends).toString());
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
            contentValues.put(TABLE_COLUMNS[41], ArrayUtils.toCommaSplitString(texts).toString());
            contentValues.put(TABLE_COLUMNS[42], ArrayUtils.toCommaSplitString(starts).toString());
            contentValues.put(TABLE_COLUMNS[43], ArrayUtils.toCommaSplitString(ends).toString());
        }

        contentValues.put(TABLE_COLUMNS[44], status.getQuotedStatusId());

        String url;
        List<Emoji> emojis;

        url = status.getRemoteUrl();
        emojis = status.getEmojis();

        contentValues.put(TABLE_COLUMNS[45], url);

        if (emojis != null){
            int size = emojis.size();
            String[] shortcodes = new String[size];
            String[] urls = new String[size];

            for(int i = 0;i < size; i++){
                Emoji emoji = emojis.get(i);
                shortcodes[i] = emoji.getShortCode();
                urls[i] = emoji.getUrl();
            }
            contentValues.put(TABLE_COLUMNS[46], ArrayUtils.toCommaSplitString(shortcodes).toString());
            contentValues.put(TABLE_COLUMNS[47], ArrayUtils.toCommaSplitString(urls).toString());
        }
        return contentValues;
    }

    public void deleteCachedStatus(long id){
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();

        SQLiteStatement statement = decrementCountStatement(database);
        statement.bindLong(1, id);
        statement.execute();

        database.delete(TABLE_NAME, "count=0", null);
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
    }

    public void deleteCachedStatuses(Collection<Long> ids){
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        SQLiteStatement sqLiteStatement = decrementCountStatement(database);
        for (Long id : ids) {
            sqLiteStatement.bindLong(1, id);
            sqLiteStatement.execute();
        }
        database.delete(TABLE_NAME, "count=0", null);
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
    }

    private SQLiteStatement decrementCountStatement(SQLiteDatabase database){
        return database.compileStatement("UPDATE " + TABLE_NAME + " SET count=count-1 WHERE id=?");
    }

    @Nullable
    private String[] splitComma(@Nullable String string){
        if (!TextUtils.isEmpty(string)) {
            return string.split(",");
        } else {
            return null;
        }
    }

    private UserMentionEntity[] restoreUserMentionEntities(String[] texts,
                                                           String[] ids,
                                                           String[] names,
                                                           String[] screenNames,
                                                           String[] starts,
                                                           String[] ends){

        if (texts != null){
            UserMentionEntity[] entities = new UserMentionEntity[texts.length];
            for (int i = 0; i < entities.length; i++){
                entities[i] = new CachedUserMentionEntity(
                        texts[i],
                        ids[i],
                        names[i],
                        screenNames[i],
                        starts[i],
                        ends[i]
                );
            }
            return entities;
        } else {
            return new UserMentionEntity[0];
        }
    }

    private static final class CachedUserMentionEntity implements UserMentionEntity {

        private String text;
        private String name;
        private String screenName;
        private String id;
        private String start;
        private String end;

        CachedUserMentionEntity(String text,
                                String id,
                                String name,
                                String screenName,
                                String start,
                                String end){
            this.text = text;
            this.id = id;
            this.name = name;
            this.screenName = screenName;
            this.start = start;
            this.end = end;

        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getScreenName() {
            return screenName;
        }

        @Override
        public long getId() {
            return Long.parseLong(id);
        }

        @Override
        public int getStart() {
            return Integer.parseInt(start);
        }

        @Override
        public int getEnd() {
            return Integer.parseInt(end);
        }
    }

    private URLEntity[] restoreURLEntities(String[] texts,
                                           String[] expandedURLs,
                                           String[] displaysURLs,
                                           String[] starts,
                                           String[] ends){

        if (texts != null){
            URLEntity[] entities = new URLEntity[texts.length];
            for (int i = 0; i < entities.length; i++){
                entities[i] = new CachedURLEntity(
                        texts[i],
                        expandedURLs[i],
                        displaysURLs[i],
                        starts[i],
                        ends[i]
                );
            }
            return entities;
        } else {
            return new URLEntity[0];
        }
    }

    private static final class CachedURLEntity implements URLEntity {

        private String text;
        private String expandedURL;
        private String displaysURL;
        private String start;
        private String end;

        CachedURLEntity(String text,
                String expandedURL,
                String displaysURL,
                String start,
                String end){
            this.text = text;
            this.expandedURL = expandedURL;
            this.displaysURL = displaysURL;
            this.start = start;
            this.end = end;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public String getURL() {
            return text;
        }

        @Override
        public String getExpandedURL() {
            return expandedURL;
        }

        @Override
        public String getDisplayURL() {
            return displaysURL;
        }

        @Override
        public int getStart() {
            return Integer.valueOf(start);
        }

        @Override
        public int getEnd() {
            return Integer.valueOf(end);
        }

    }

    private HashtagEntity[] restoreHashtagEntities(String[] texts,
                                               String[] starts,
                                               String[] ends){

        if (texts != null){
            HashtagEntity[] entities = new HashtagEntity[texts.length];
            for (int i = 0; i < entities.length; i++){
                entities[i] = new CachedHashtagEntity(
                        texts[i],
                        starts[i],
                        ends[i]
                );
            }
            return entities;
        } else {
            return new HashtagEntity[0];
        }
    }

    private static final class CachedHashtagEntity implements HashtagEntity {
        private String text;
        private String start;
        private String end;

        CachedHashtagEntity(String text,
                String start,
                String end){
            this.text = text;
            this.start = start;
            this.end = end;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public int getStart() {
            return Integer.parseInt(start);
        }

        @Override
        public int getEnd() {
            return Integer.parseInt(end);
        }
    }

    private MediaEntity[] restoreMediaEntities(String[] texts,
                                               String[] expandedURLs,
                                               String[] displaysURLs,
                                               String[] ids,
                                               String[] mediaUrls,
                                               String[] mediaUrlHttpSs,
                                               String[] types,
                                               String[][] variants_bitrates,
                                               String[][] variants_contentTypes,
                                               String[][] variants_urls,
                                               String[] starts,
                                               String[] ends){

        if (texts != null){
            MediaEntity[] entities = new MediaEntity[texts.length];
            for (int i = 0; i < entities.length; i++){
                boolean hasMedia = variants_bitrates != null && variants_bitrates.length != 0;
                entities[i] = new CachedMediaEntity(
                        texts[i],
                        expandedURLs[i],
                        displaysURLs[i],
                        ids[i],
                        mediaUrls[i],
                        mediaUrlHttpSs[i],
                        types[i],
                        hasMedia? variants_bitrates[i]: null,
                        hasMedia? variants_contentTypes[i]: null,
                        hasMedia? variants_urls[i]: null,
                        starts[i],
                        ends[i]
                );
            }
            return entities;
        } else {
            return new MediaEntity[0];
        }
    }

    private static final class CachedMediaEntity implements MediaEntity{

        private final String text;
        private final String expandedURL;
        private final String displaysURL;
        private final String id;
        private final String mediaUrl;
        private final String mediaUrlHttps;
        private final String mediaType;
        private final String[] variants_bitrate;
        private final String[] variants_contentType;
        private final String[] variants_url;
        private final String start;
        private final String end;

        CachedMediaEntity(String text,
                String expandedURL,
                String displaysURL,
                String id,
                String mediaUrl,
                String mediaUrlHttps,
                String mediaType,
                String[] variants_bitrate,
                String[] variants_contentType,
                String[] variants_url,
                String start,
                String end){

            this.text = text;
            this.expandedURL = expandedURL;
            this.displaysURL = displaysURL;
            this.id = id;
            this.mediaUrl = mediaUrl;
            this.mediaUrlHttps = mediaUrlHttps;
            this.mediaType = mediaType;
            this.variants_bitrate = variants_bitrate;
            this.variants_contentType = variants_contentType;
            this.variants_url = variants_url;
            this.start = start;
            this.end = end;
        }

        @Override
        public long getId() {
            return Long.parseLong(id);
        }

        @Override
        public String getMediaURL() {
            return mediaUrl;
        }

        @Override
        public String getMediaURLHttps() {
            return mediaUrlHttps;
        }

        @Override
        public Map<Integer, MediaEntity.Size> getSizes() {
            return null;
        }

        @Override
        public String getType() {
            return mediaType;
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
        public MediaEntity.Variant[] getVideoVariants() {
            if (variants_url != null) {
                MediaEntity.Variant[] result = new MediaEntity.Variant[variants_url.length];
                for (int ii = 0; ii < variants_url.length; ii++) {
                    result[ii] = new Variant(
                            variants_bitrate[ii],
                            variants_contentType[ii],
                            variants_url[ii]);
                }
                return result;
            } else {
                return null;
            }
        }

        @Override
        public String getExtAltText() {
            return null;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public String getURL() {
            return text;
        }

        @Override
        public String getExpandedURL() {
            return expandedURL;
        }

        @Override
        public String getDisplayURL() {
            return displaysURL;
        }

        @Override
        public int getStart() {
            return Integer.parseInt(start);
        }

        @Override
        public int getEnd() {
            return Integer.parseInt(end);
        }

        private static final class Variant implements MediaEntity.Variant {
            private String bitrate;
            private String contentType;
            private String url;

            Variant(String bitrate,
                    String contentType,
                    String url){
                this.bitrate = bitrate;
                this.contentType = contentType;
                this.url = url;
            }

            @Override
            public int getBitrate() {
                return Integer.parseInt(bitrate);
            }

            @Override
            public String getContentType() {
                return contentType;
            }

            @Override
            public String getUrl() {
                return url;
            }
        }
    }

    @Nullable
    private String[][] parse(@Nullable String string){
        if (TextUtils.isEmpty(string)){
            return null;
        }
        String[] resultA = string.split(",");
        if (resultA.length == 1 && resultA[0].equals("")){
            return null;
        }
        String[][] result = new String[resultA.length][];
        for (int i = 0; i < resultA.length; i++) {
            result[i] = resultA[i].split("\\|");
        }
        return result;
    }

    private SymbolEntity[] restoreSymbolEntities(String[] texts,
                                                  String[] starts,
                                                  String[] ends){

        if (texts != null){
            SymbolEntity[] entities = new SymbolEntity[texts.length];
            for (int i = 0; i < entities.length; i++){
                entities[i] = new CachedSymbolEntity(
                        texts[i],
                        starts[i],
                        ends[i]
                );
            }
            return entities;
        } else {
            return new SymbolEntity[0];
        }
    }

    private static final class CachedSymbolEntity implements SymbolEntity {
        private String text;
        private String start;
        private String end;

        CachedSymbolEntity(String text,
                            String start,
                            String end){
            this.text = text;
            this.start = start;
            this.end = end;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public int getStart() {
            return Integer.parseInt(start);
        }

        @Override
        public int getEnd() {
            return Integer.parseInt(end);
        }
    }

    @Nullable
    private List<Emoji> restoreEmojis(@Nullable String[] shortcodes,
                                              @Nullable String[] urls){

        if (shortcodes != null && urls != null) {
            List<Emoji> emojis = new ArrayList<>(shortcodes.length);
            for (int i = 0; i < shortcodes.length; i++) {
                emojis.add(new Emoji(shortcodes[i], urls[i]));
            }

            return emojis;
        } else {
            return null;
        }
    }
}