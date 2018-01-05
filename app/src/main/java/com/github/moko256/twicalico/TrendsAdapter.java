/*
 * Copyright 2017 The twicalico authors
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
    private List<Trend> data;
    private Context context;

    TrendsAdapter(Context context, List<Trend> data) {
        this.context = context;
        this.data = data;

        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).getName().hashCode();
    }

    @Override
    public TrendsAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new TrendsAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_trend, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(TrendsAdapter.ViewHolder viewHolder, final int i) {
        Trend item=data.get(i);

        viewHolder.text.setText(item.getName());
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

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView text;

        ViewHolder(final View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.trend_text);
        }
    }
}
