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

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import twitter4j.Status;

/**
 * Created by moko256 on 2016/02/11.
 *
 * @author moko256
 */
class StatusesAdapter extends RecyclerView.Adapter<StatusesAdapter.ViewHolder> {

    private ArrayList<Long> data;
    private Context context;

    StatusesAdapter(Context context, ArrayList<Long> data) {
        this.context = context;
        this.data = data;

        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return data.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        Status status=GlobalApplication.statusCache.get(data.get(position));
        Status item = status.isRetweet()?status.getRetweetedStatus():status;
        AppConfiguration conf=GlobalApplication.configuration;
        if((conf.isPatternTweetMuteEnabled() && item.getText().matches(conf.getTweetMutePattern())) ||
                (conf.isPatternUserScreenNameMuteEnabled() && item.getUser().getScreenName().matches(conf.getUserScreenNameMutePattern())) ||
                (conf.isPatternUserNameMuteEnabled() && item.getUser().getName().matches(conf.getUserNameMutePattern())) ||
                (conf.isPatternTweetSourceMuteEnabled() && item.getSource().matches(conf.getTweetSourceMutePattern()))
                ){
            return 1;
        }
        return super.getItemViewType(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        /*
        if (i==1){
            ViewHolder vh=new ViewHolder(inflater.inflate(R.layout.layout_tweet, viewGroup, false));
            vh.tweetContext.setTextColor(context.getResources().getColor(R.color.imageToggleDisable));
            vh.tweetContext.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            return vh;
        }
        */
        return new ViewHolder(new StatusView(context));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int i) {
        viewHolder.statusView.setStatus(GlobalApplication.statusCache.get(data.get(i)));
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.statusView.setStatus(null);
    }

    @Override
    public int getItemCount() {
        if (data != null) {
            return data.size();
        } else {
            return 0;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        StatusView statusView;

        ViewHolder(final View itemView) {
            super(itemView);

            statusView= (StatusView) itemView;
        }
    }
}
