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
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.github.moko256.twicalico.config.AppConfiguration;
import com.github.moko256.twicalico.text.TwitterStringUtils;
import com.github.moko256.twicalico.widget.TweetImageTableView;

import java.util.ArrayList;

import twitter4j.Status;

/**
 * Created by moko256 on 2016/02/11.
 *
 * @author moko256
 */
class StatusesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Long> data;
    private Context context;
    private OnLoadMoreClickListener onLoadMoreClick;

    StatusesAdapter(Context context, ArrayList<Long> data) {
        this.context = context;
        this.data = data;

        setHasStableIds(true);
    }

    public void setOnLoadMoreClick(OnLoadMoreClickListener onLoadMoreClick) {
        this.onLoadMoreClick = onLoadMoreClick;
    }

    public OnLoadMoreClickListener getOnLoadMoreClick() {
        return onLoadMoreClick;
    }

    @Override
    public long getItemId(int position) {
        return data.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (data.get(position) == -1L){
            return 1;
        }
        Status status=GlobalApplication.statusCache.get(data.get(position));
        Status item = status.isRetweet()?status.getRetweetedStatus():status;
        AppConfiguration conf=GlobalApplication.configuration;
        if((conf.isPatternTweetMuteEnabled() && item.getText().matches(conf.getTweetMutePattern())) ||
                (conf.isPatternUserScreenNameMuteEnabled() && conf.getUserScreenNameMutePattern().matcher(item.getUser().getScreenName()).matches()) ||
                (conf.isPatternUserNameMuteEnabled() && conf.getUserNameMutePattern().matcher(item.getUser().getName()).matches()) ||
                (conf.isPatternTweetSourceMuteEnabled() && conf.getTweetSourceMutePattern().matcher(TwitterStringUtils.removeHtmlTags(item.getSource())).matches())
                ){
            return 2;
        }
        if ((conf.isPatternTweetMuteShowOnlyImageEnabled()
                && item.getMediaEntities().length > 0
                && conf.getTweetMuteShowOnlyImagePattern().matcher(item.getText()).matches())){
            return 3;
        }
        return super.getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (i == 1){
            return new MoreLoadViewHolder();
        } else if (i == 2) {
            return new MutedTweetViewHolder();
        } else if (i == 3){
            return new ImagesOnlyTweetViewHolder();
        } else {
            return new StatusViewHolder();
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int i) {
        if (viewHolder instanceof StatusViewHolder) {
            ((StatusViewHolder) viewHolder).setStatus(GlobalApplication.statusCache.get(data.get(i)));
        } else if (viewHolder instanceof ImagesOnlyTweetViewHolder){
            ((ImagesOnlyTweetViewHolder) viewHolder).setStatus(GlobalApplication.statusCache.get(data.get(i)));
        } else if (viewHolder instanceof MoreLoadViewHolder) {
            viewHolder.itemView.setOnClickListener(v -> onLoadMoreClick.onClick(i));
            ViewGroup.LayoutParams oldParams = viewHolder.itemView.getLayoutParams();
            StaggeredGridLayoutManager.LayoutParams params = oldParams != null?
                    new StaggeredGridLayoutManager.LayoutParams(oldParams) :
                    new StaggeredGridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setFullSpan(true);
            viewHolder.itemView.setLayoutParams(params);
        }
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof StatusViewHolder){
            ((StatusViewHolder) holder).setStatus(null);
        }
    }

    @Override
    public int getItemCount() {
        if (data != null) {
            return data.size();
        } else {
            return 0;
        }
    }

    private class StatusViewHolder extends RecyclerView.ViewHolder {
        StatusView statusView;

        StatusViewHolder() {
            super(new StatusView(context));
            statusView = (StatusView) itemView;
        }

        void setStatus(Status status) {
            statusView.setStatus(status);
        }
    }

    private class MoreLoadViewHolder extends RecyclerView.ViewHolder {
        MoreLoadViewHolder() {
            super(LayoutInflater.from(context).inflate(R.layout.layout_list_load_more_text, null));
        }
    }

    private class MutedTweetViewHolder extends RecyclerView.ViewHolder {
        MutedTweetViewHolder() {
            super(LayoutInflater.from(context).inflate(R.layout.layout_list_muted_text, null));
        }
    }

    private class ImagesOnlyTweetViewHolder extends RecyclerView.ViewHolder {
        TweetImageTableView tweetImageTableView;

        ImagesOnlyTweetViewHolder() {
            super(LayoutInflater.from(context).inflate(R.layout.layout_list_tweet_only_image, null));
            tweetImageTableView = itemView.findViewById(R.id.list_tweet_image_container);
        }

        void setStatus(Status status) {
            Status item = status.isRetweet()? status.getRetweetedStatus(): status;
            tweetImageTableView.setMediaEntities(item.getMediaEntities());
            itemView.setOnLongClickListener(v -> {
                context.startActivity(ShowTweetActivity.getIntent(context, item.getId()));
                return true;
            });
        }
    }

    interface OnLoadMoreClickListener {
        void onClick(int position);
    }
}
