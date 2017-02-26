package com.github.moko256.twicalico;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Place;
import twitter4j.RateLimitStatus;
import twitter4j.Scopes;
import twitter4j.Status;
import twitter4j.SymbolEntity;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;

/**
 * Created by moko256 on 2016/12/22.
 *
 * @author moko256
 */

public class StatusCacheMap {

    private HashMap<Long, Status> statusCache=new HashMap<>();
    private UserCacheMap userCache;

    public StatusCacheMap(UserCacheMap userCache){
        this.userCache=userCache;
    }

    public int size() {
        return statusCache.size();
    }

    public boolean isEmpty() {
        return statusCache.isEmpty();
    }

    public void add(final Status status) {
        statusCache.put(status.getId(), new Status() {
            /* Based on twitter4j.StatusJSONImpl */
            private Date createdAt=new Date(status.getCreatedAt().getTime());
            private long id=status.getId();
            private String text=status.getText();
            private String source=status.getSource();
            private boolean isTruncated=status.isTruncated();
            private long inReplyToStatusId=status.getInReplyToStatusId();
            private long inReplyToUserId=status.getInReplyToUserId();
            private boolean isFavorited=status.isFavorited();
            private boolean isRetweeted=status.isRetweeted();
            private int favoriteCount=status.getFavoriteCount();
            private String inReplyToScreenName=status.getInReplyToScreenName();
            private GeoLocation geoLocation = status.getGeoLocation();
            private Place place = status.getPlace();

            private int retweetCount=status.getRetweetCount();
            private boolean isPossiblySensitive=status.isPossiblySensitive();
            private String lang=status.getLang();

            private long[] contributorsIDs=status.getContributors();

            private Status retweetedStatus=status.getRetweetedStatus();
            private UserMentionEntity[] userMentionEntities=status.getUserMentionEntities();
            private URLEntity[] urlEntities=status.getURLEntities();
            private HashtagEntity[] hashtagEntities=status.getHashtagEntities();
            private MediaEntity[] mediaEntities=status.getMediaEntities();
            private SymbolEntity[] symbolEntities=status.getSymbolEntities();
            private long currentUserRetweetId = status.getCurrentUserRetweetId();
            private Scopes scopes=status.getScopes();
            private String[] withheldInCountries = status.getWithheldInCountries();
            private Status quotedStatus=status.getQuotedStatus();
            private long quotedStatusId = status.getQuotedStatusId();

            private int displayTextRangeStart = status.getDisplayTextRangeStart();
            private int displayTextRangeEnd = status.getDisplayTextRangeEnd();



            @Override
            public Date getCreatedAt() {
                return createdAt;
            }

            @Override
            public long getId() {
                return id;
            }

            @Override
            public String getText() {
                return text;
            }

            @Override
            public String getSource() {
                return source;
            }

            @Override
            public boolean isTruncated() {
                return isTruncated;
            }

            @Override
            public long getInReplyToStatusId() {
                return inReplyToStatusId;
            }

            @Override
            public long getInReplyToUserId() {
                return inReplyToUserId;
            }

            @Override
            public String getInReplyToScreenName() {
                return inReplyToScreenName;
            }

            @Override
            public GeoLocation getGeoLocation() {
                return geoLocation;
            }

            @Override
            public Place getPlace() {
                return place;
            }

            @Override
            public boolean isFavorited() {
                return isFavorited;
            }

            @Override
            public boolean isRetweeted() {
                return isRetweeted;
            }

            @Override
            public int getFavoriteCount() {
                return favoriteCount;
            }

            @Override
            public User getUser() {
                return userCache.get(status.getUser().getId());
            }

            @Override
            public boolean isRetweet() {
                return retweetedStatus!=null;
            }

            @Override
            public Status getRetweetedStatus() {
                return retweetedStatus;
            }

            @Override
            public long[] getContributors() {
                return contributorsIDs;
            }

            @Override
            public int getRetweetCount() {
                return retweetCount;
            }

            @Override
            public boolean isRetweetedByMe() {
                return currentUserRetweetId != -1L;
            }

            @Override
            public long getCurrentUserRetweetId() {
                return currentUserRetweetId;
            }

            @Override
            public boolean isPossiblySensitive() {
                return isPossiblySensitive;
            }

            @Override
            public String getLang() {
                return lang;
            }

            @Override
            public Scopes getScopes() {
                return scopes;
            }

            @Override
            public String[] getWithheldInCountries() {
                return withheldInCountries;
            }

            @Override
            public long getQuotedStatusId() {
                return quotedStatusId;
            }

            @Override
            public Status getQuotedStatus() {
                return quotedStatus;
            }

            @Override
            public int getDisplayTextRangeStart() {
                return displayTextRangeStart;
            }

            @Override
            public int getDisplayTextRangeEnd() {
                return displayTextRangeEnd;
            }

            @Override
            public int compareTo(Status o) {
                return 0;
            }

            @Override
            public UserMentionEntity[] getUserMentionEntities() {
                return userMentionEntities;
            }

            @Override
            public URLEntity[] getURLEntities() {
                return urlEntities;
            }

            @Override
            public HashtagEntity[] getHashtagEntities() {
                return hashtagEntities;
            }

            @Override
            public MediaEntity[] getMediaEntities() {
                return mediaEntities;
            }

            @Override
            public SymbolEntity[] getSymbolEntities() {
                return symbolEntities;
            }

            @Override
            public RateLimitStatus getRateLimitStatus() {
                return null;
            }

            @Override
            public int getAccessLevel() {
                return 0;
            }
        });
    }

    public void addAll(Collection<? extends Status> c) {
        for (Status status : c) {
            add(status);
        }
    }

    public void clear() {

    }
}