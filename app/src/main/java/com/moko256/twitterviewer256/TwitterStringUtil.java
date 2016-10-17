package com.moko256.twitterviewer256;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.Toast;

import java.util.List;

import rx.Observable;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.SymbolEntity;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

/**
 * Created by moko256 on 2016/08/06.
 *
 * @author moko256
 */

public class TwitterStringUtil {

    @NonNull
    public static String plusAtMark(String string){
        return (new StringBuilder("@")).append(string).toString();
    }

    public static CharSequence getLinkedSequence(Status item, Context mContext){

        String tweet = item.getText();

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(tweet);

        for (SymbolEntity symbolEntity : item.getSymbolEntities()) {
            spannableStringBuilder.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(mContext, symbolEntity.toString(), Toast.LENGTH_SHORT).show();
                }
            }, tweet.offsetByCodePoints(0,symbolEntity.getStart()), tweet.offsetByCodePoints(0,symbolEntity.getEnd()), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

        for (HashtagEntity hashtagEntity : item.getHashtagEntities()) {
            spannableStringBuilder.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(mContext, hashtagEntity.toString(), Toast.LENGTH_SHORT).show();
                }
            }, tweet.offsetByCodePoints(0,hashtagEntity.getStart()), tweet.offsetByCodePoints(0,hashtagEntity.getEnd()), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

        for (UserMentionEntity userMentionEntity : item.getUserMentionEntities()) {
            spannableStringBuilder.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, ShowUserActivity.class);
                    intent.putExtra("userName", userMentionEntity.getScreenName());
                    mContext.startActivity(intent);
                }
            }, tweet.offsetByCodePoints(0,userMentionEntity.getStart()), tweet.offsetByCodePoints(0,userMentionEntity.getEnd()), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

        List<URLEntity> urlEntities=Observable
                .concat(Observable.from(item.getURLEntities()),Observable.from(item.getMediaEntities()))
                .toSortedList((i1,i2)->i1.getStart()-i2.getStart())
                .toBlocking()
                .single();

        int sp = 0;
        boolean isQuoted=item.getQuotedStatusId()!=-1;
        String empty="";

        for (URLEntity entity : urlEntities) {
            String url = entity.getURL();
            String expandedUrl = entity.getExpandedURL();
            boolean isQuotedStatusUrl=isQuoted && expandedUrl.equals("https://twitter.com/" + item.getQuotedStatus().getUser().getScreenName() + "/status/" + item.getQuotedStatusId());
            String displayUrl =((entity instanceof MediaEntity)||isQuotedStatusUrl)? empty : entity.getDisplayURL();


            int urlLength = url.codePointCount(0, url.length());
            int displayUrlLength = displayUrl.codePointCount(0, displayUrl.length());
            int dusp = displayUrlLength - urlLength;
            spannableStringBuilder.replace(tweet.offsetByCodePoints(0,entity.getStart()) + sp, tweet.offsetByCodePoints(0,entity.getEnd()) + sp, displayUrl);
            if(!displayUrl.equals(empty)){
                spannableStringBuilder.setSpan(new URLSpan(expandedUrl), tweet.offsetByCodePoints(0,entity.getStart()) + sp, tweet.offsetByCodePoints(0,entity.getEnd()) + sp + dusp, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
            sp+=dusp;
        }

        return spannableStringBuilder;

    }

}
