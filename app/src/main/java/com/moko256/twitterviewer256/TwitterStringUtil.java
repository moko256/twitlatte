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
        return "@"+string;
    }

    public static CharSequence getLinkedSequence(Status item, Context mContext){
        SpannableStringBuilder spannableStringBuilder=new SpannableStringBuilder(item.getText());

        for (SymbolEntity symbolEntity : item.getSymbolEntities()) {
            spannableStringBuilder.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(mContext, symbolEntity.toString(), Toast.LENGTH_SHORT).show();
                }
            },symbolEntity.getStart(),symbolEntity.getEnd(),Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

        for (HashtagEntity hashtagEntity : item.getHashtagEntities()) {
            spannableStringBuilder.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(mContext, hashtagEntity.toString(), Toast.LENGTH_SHORT).show();
                }
            },hashtagEntity.getStart(),hashtagEntity.getEnd(),Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

        for (UserMentionEntity userMentionEntity : item.getUserMentionEntities()) {
            spannableStringBuilder.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(mContext,ShowUserActivity.class);
                    intent.putExtra("userName",userMentionEntity.getScreenName());
                    mContext.startActivity(intent);
                }
            },userMentionEntity.getStart(),userMentionEntity.getEnd(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

        int sp=0;
        for (URLEntity entity : item.getURLEntities()) {
            int urlLength=entity.getURL().length();
            int displayUreLength=entity.getDisplayURL().length();
            int dusp=displayUreLength-urlLength;
            spannableStringBuilder.replace(entity.getStart()+sp,entity.getEnd()+sp,entity.getDisplayURL());
            spannableStringBuilder.setSpan(new URLSpan(entity.getExpandedURL()),entity.getStart()+sp,entity.getEnd()+sp+dusp,Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

        return spannableStringBuilder;
    }

}
