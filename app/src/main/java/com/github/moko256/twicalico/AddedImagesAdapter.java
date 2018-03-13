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
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by moko256 on 2017/03/11.
 *
 * @author moko256
 */

public class AddedImagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_IMAGE = 1;
    private static final int VIEW_TYPE_ADD = 2;

    private Context context;

    private ArrayList<Uri> images = new ArrayList<>();
    private int limit = -1;

    private View.OnClickListener onAddButtonClickListener;

    public AddedImagesAdapter(Context context){
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        return ((limit == -1) || ((position < limit))) && (position < images.size())? VIEW_TYPE_IMAGE: VIEW_TYPE_ADD;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_IMAGE){
            return new ImageChildViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_images_adapter_image_child, parent, false));
        } else {
            return new AddImageViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_images_adapter_add_image, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_IMAGE){
            ImageChildViewHolder viewHolder = (ImageChildViewHolder) holder;
            Uri image = images.get(position);

            String fileName = null;
            Cursor cursor = context.getContentResolver().query(
                    image,
                    new String[]{MediaStore.MediaColumns.DISPLAY_NAME},
                    null, null, null);
            if (cursor != null && cursor.moveToNext()){
                fileName = cursor.getString(
                        cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                );
            }
            if (cursor != null) {
                cursor.close();
            }

            viewHolder.title.setText(fileName != null? fileName : image.getLastPathSegment());
            GlideApp.with(context).load(image).into(viewHolder.image);
        } else {
            holder.itemView.setOnClickListener(onAddButtonClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return (limit == -1 || images.size()<limit)? images.size() + 1: limit;
    }


    @Override
    public long getItemId(int position) {
        if (position < images.size()){
            return images.get(position).hashCode();
        } else {
            return -1;
        }
    }

    public View.OnClickListener getOnAddButtonClickListener() {
        return onAddButtonClickListener;
    }

    public void setOnAddButtonClickListener(View.OnClickListener onAddButtonClickListener) {
        this.onAddButtonClickListener = onAddButtonClickListener;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public ArrayList<Uri> getImagesList() {
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
}
