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

package com.github.moko256.twitlatte;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import twitter4j.Trend;

/**
 * Created by moko256 on 2017/07/05.
 *
 * @author moko256
 */

public class TrendsAdapter extends RecyclerView.Adapter<TrendsAdapter.ViewHolder> {
    private final List<Trend> data;
    private final Context context;

    TrendsAdapter(Context context, List<Trend> data) {
        this.context = context;
        this.data = data;

        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).getName().hashCode();
    }

    @NonNull
    @Override
    public TrendsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new TrendsAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_trend, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TrendsAdapter.ViewHolder viewHolder, final int i) {
        Trend item=data.get(i);

        viewHolder.text.setText(item.getName());
        if (item.getTweetVolume() != -1) {
            if (viewHolder.volume.getVisibility() != View.VISIBLE) {
                viewHolder.volume.setVisibility(View.VISIBLE);
            }
            viewHolder.volume.setText(context.getString(R.string.tweet_per_last_24_hours, item.getTweetVolume()));
        } else {
            if (viewHolder.volume.getVisibility() != View.GONE) {
                viewHolder.volume.setVisibility(View.GONE);
            }
        }
        viewHolder.itemView.setOnClickListener(v -> context.startActivity(SearchResultActivity.getIntent(context, item.getName())));

    }

    @Override
    public int getItemCount() {
        if (data != null) {
            return data.size();
        } else {
            return 0;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView text;
        final TextView volume;

        ViewHolder(final View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.trend_text);
            volume = itemView.findViewById(R.id.trend_text_volume);
        }
    }
}
