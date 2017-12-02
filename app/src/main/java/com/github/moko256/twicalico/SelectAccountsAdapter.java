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
import android.net.Uri;
import android.support.annotation.StringRes;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by moko256 on 2017/10/26.
 *
 * @author moko256
 */

public class SelectAccountsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_IMAGE = 1;
    private static final int VIEW_TYPE_ADD = 2;

    Context context;

    private ArrayList<Pair<Uri, String>> images = new ArrayList<>();

    private View.OnClickListener onAddButtonClickListener;
    private OnImageClickListener onImageButtonClickListener;

    public SelectAccountsAdapter(Context context){
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        return (position < images.size())? VIEW_TYPE_IMAGE: VIEW_TYPE_ADD;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_IMAGE){
            return new ImageChildViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_select_accounts_image_child, parent, false));
        } else {
            return new AddImageViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_select_accounts_add_image, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_IMAGE){
            ImageChildViewHolder viewHolder = (ImageChildViewHolder) holder;
            Pair<Uri, String> pair = images.get(position);
            Uri image = pair.first;
            viewHolder.title.setText(pair.second);
            GlideApp.with(context).load(image).circleCrop().into(viewHolder.image);
            viewHolder.itemView.setOnClickListener(v -> {
                if (onImageButtonClickListener != null){
                    onImageButtonClickListener.onClick(position);
                }
            });
        } else {
            holder.itemView.setOnClickListener(onAddButtonClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return images.size() + 1;
    }


    public View.OnClickListener getOnAddButtonClickListener() {
        return onAddButtonClickListener;
    }

    public void setOnAddButtonClickListener(View.OnClickListener onAddButtonClickListener) {
        this.onAddButtonClickListener = onAddButtonClickListener;
    }

    public OnImageClickListener getOnImageButtonClickListener() {
        return onImageButtonClickListener;
    }

    public void setOnImageButtonClickListener(OnImageClickListener onImageButtonClickListener) {
        this.onImageButtonClickListener = onImageButtonClickListener;
    }

    public ArrayList<Pair<Uri, String>> getImagesList() {
        return images;
    }

    public void clearImages() {
        images.clear();
        images = null;
    }

    class ImageChildViewHolder extends RecyclerView.ViewHolder{

        ImageView image;
        TextView title;

        ImageChildViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.layout_images_adapter_image_child_image);
            title = itemView.findViewById(R.id.layout_images_adapter_image_child_title);
        }
    }

    class AddImageViewHolder extends RecyclerView.ViewHolder{

        AddImageViewHolder(View itemView) {
            super(itemView);
        }
    }

    public interface OnImageClickListener {
        void onClick(int index);
    }
}
