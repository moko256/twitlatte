package com.moko256.twitterviewer256;

import android.content.Context;
import android.content.Intent;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.Toast;

import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.SymbolEntity;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

/**
 * Created by moko256 on GitHub on 2016/08/06.
 */
public class TwitterStringUtil {
    public static String plusAtMark(String string){
        return (new StringBuilder("@")).append(string).toString();
    }

    public static CharSequence getLinkedSequence(Status item, Context mContext){

        try {

            String tweet = item.getText();
            int tweetLength = tweet.length();
            int tweetCodePointCount = tweet.codePointCount(0, tweetLength);

            int charPositions[] = new int[tweetCodePointCount + 1];
            charPositions[0] = 0;
            int ii = 1;
            for (int i = 1, cp; i < tweetLength + 1; i += Character.charCount(cp)) {
                cp = tweet.codePointAt(i - 1);
                charPositions[ii] = i;
                ii++;
            }

            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(tweet);

            for (SymbolEntity symbolEntity : item.getSymbolEntities()) {
                spannableStringBuilder.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(mContext, symbolEntity.toString(), Toast.LENGTH_SHORT).show();
                    }
                }, charPositions[symbolEntity.getStart()], charPositions[symbolEntity.getEnd()], Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }

            for (HashtagEntity hashtagEntity : item.getHashtagEntities()) {
                spannableStringBuilder.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(mContext, hashtagEntity.toString(), Toast.LENGTH_SHORT).show();
                    }
                }, charPositions[hashtagEntity.getStart()], charPositions[hashtagEntity.getEnd()], Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }

            for (UserMentionEntity userMentionEntity : item.getUserMentionEntities()) {
                spannableStringBuilder.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, ShowUserActivity.class);
                        intent.putExtra("userName", userMentionEntity.getScreenName());
                        mContext.startActivity(intent);
                    }
                }, charPositions[userMentionEntity.getStart()], charPositions[userMentionEntity.getEnd()], Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }

            int sp = 0;
            for (URLEntity entity : item.getURLEntities()) {
                String url = entity.getURL();
                String displayUrl = entity.getDisplayURL();
                int urlLength = url.codePointCount(0, url.length());
                int displayUreLength = displayUrl.codePointCount(0, displayUrl.length());
                int dusp = displayUreLength - urlLength;
                spannableStringBuilder.replace(charPositions[entity.getStart()] + sp, charPositions[entity.getEnd()] + sp, entity.getDisplayURL());
                spannableStringBuilder.setSpan(new URLSpan(entity.getExpandedURL()), charPositions[entity.getStart()] + sp, charPositions[entity.getEnd()] + sp + dusp, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }

            return spannableStringBuilder;

        }
        catch (IndexOutOfBoundsException e){
            e.printStackTrace();
            return item.getText();
        }
    }

}
