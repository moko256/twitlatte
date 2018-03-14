/*
 * Copyright 2018 The twicalico authors
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
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.moko256.twicalico.config.AppConfiguration;
import com.github.moko256.twicalico.widget.TweetImageTableView;

import java.util.List;

import twitter4j.Status;

/**
 * Created by moko256 on 2016/02/11.
 *
 * @author moko256
 */
class StatusesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Long> data;
    private Context context;
    private OnLoadMoreClickListener onLoadMoreClick;
    private boolean shouldShowMediaOnly = false;

    StatusesAdapter(Context context, List<Long> data) {
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

    public boolean shouldShowMediaOnly() {
        return shouldShowMediaOnly;
    }

    public void setShouldShowMediaOnly(boolean shouldShowMediaOnly) {
        this.shouldShowMediaOnly = shouldShowMediaOnly;
    }

    @Override
    public long getItemId(int position) {
        return data.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (data.get(position) == -1L ){
            return 1;
        }
        Status status=GlobalApplication.statusCache.get(data.get(position));
        if (status == null){
            return 1;
        }
        Status item = status.isRetweet()?status.getRetweetedStatus():status;
        if (item == null){
            return 1;
        }
        AppConfiguration conf=GlobalApplication.configuration;
        if((conf.isPatternTweetMuteEnabled() && conf.getTweetMutePattern().matcher(item.getText()).find()) ||
                (conf.isPatternUserScreenNameMuteEnabled() && conf.getUserScreenNameMutePattern().matcher(item.getUser().getScreenName()).find()) ||
                (conf.isPatternUserNameMuteEnabled() && conf.getUserNameMutePattern().matcher(item.getUser().getName()).find()) ||
                (conf.isPatternTweetSourceMuteEnabled() && conf.getTweetSourceMutePattern().matcher(item.getSource()).find())
                ){
            return 2;
        }
        if (shouldShowMediaOnly || (conf.isPatternTweetMuteShowOnlyImageEnabled()
                && item.getMediaEntities().length > 0
                && conf.getTweetMuteShowOnlyImagePattern().matcher(item.getText()).find())) {
            return 3;
        }
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (i == 1){
            return new MoreLoadViewHolder(viewGroup);
        } else if (i == 2) {
            return new MutedTweetViewHolder(viewGroup);
        } else if (i == 3){
            return new ImagesOnlyTweetViewHolder(viewGroup);
        } else {
            return new StatusViewHolder();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
        if (viewHolder instanceof StatusViewHolder) {
            ((StatusViewHolder) viewHolder).setStatus(GlobalApplication.statusCache.get(data.get(i)));
        } else if (viewHolder instanceof ImagesOnlyTweetViewHolder){
            ((ImagesOnlyTweetViewHolder) viewHolder).setStatus(GlobalApplication.statusCache.get(data.get(i)));
        } else if (viewHolder instanceof MoreLoadViewHolder) {
            ((MoreLoadViewHolder) viewHolder).setIsLoading(false);
            viewHolder.itemView.setOnClickListener(v -> {
                ((MoreLoadViewHolder) viewHolder).setIsLoading(true);
                onLoadMoreClick.onClick(i);
            });
            ViewGroup.LayoutParams oldParams = viewHolder.itemView.getLayoutParams();
            StaggeredGridLayoutManager.LayoutParams params = oldParams != null?
                    new StaggeredGridLayoutManager.LayoutParams(oldParams) :
                    new StaggeredGridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setFullSpan(true);
            viewHolder.itemView.setLayoutParams(params);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
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
        TextView text;
        ProgressBar progressBar;

        private boolean isLoading = false;

        MoreLoadViewHolder(ViewGroup viewGroup) {
            super(LayoutInflater.from(context).inflate(R.layout.layout_list_load_more_text, viewGroup, false));
            text = itemView.findViewById(R.id.layout_list_load_more_text_view);
            progressBar = itemView.findViewById(R.id.layout_list_load_more_text_progress);
        }

        void setIsLoading(boolean isLoading){
            this.isLoading = isLoading;
            itemView.setClickable(!isLoading);
            text.setVisibility(isLoading? View.INVISIBLE: View.VISIBLE);
            progressBar.setVisibility(isLoading? View.VISIBLE: View.INVISIBLE);
        }

        boolean getIsLoading(){
            return isLoading;
        }
    }

    private class MutedTweetViewHolder extends RecyclerView.ViewHolder {
        MutedTweetViewHolder(ViewGroup viewGroup) {
            super(LayoutInflater.from(context).inflate(R.layout.layout_list_muted_text, viewGroup, false));
        }
    }

    private class ImagesOnlyTweetViewHolder extends RecyclerView.ViewHolder {
        TweetImageTableView tweetImageTableView;

        ImagesOnlyTweetViewHolder(ViewGroup viewGroup) {
            super(LayoutInflater.from(context).inflate(R.layout.layout_list_tweet_only_image, viewGroup, false));
            tweetImageTableView = itemView.findViewById(R.id.list_tweet_image_container);
        }

        void setStatus(Status status) {
            Status item = status.isRetweet()? status.getRetweetedStatus(): status;
            tweetImageTableView.setMediaEntities(item.getMediaEntities(), item.isPossiblySensitive());
            tweetImageTableView.setOnLongClickListener(v -> {
                context.startActivity(ShowTweetActivity.getIntent(context, item.getId()));
                return true;
            });
        }
    }

    interface OnLoadMoreClickListener {
        void onClick(int position);
    }
}
