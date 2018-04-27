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
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by moko256 on 2017/03/11.
 *
 * @author moko256
 */

public class AddedImagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;

    private ArrayList<Uri> images = new ArrayList<>();
    public int limit = 4;

    public View.OnClickListener onAddButtonClickListener;
    public ImageAction onDeleteButtonListener;
    public ImageAction onImageClickListener;

    public AddedImagesAdapter(Context context){
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        return position < limit && position < images.size() ? R.layout.layout_images_adapter_image_child: R.layout.layout_images_adapter_add_image;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == R.layout.layout_images_adapter_image_child){
            return new ImageChildViewHolder(LayoutInflater.from(context).inflate(viewType, parent, false));
        } else {
            return new AddImageViewHolder(LayoutInflater.from(context).inflate(viewType, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ImageChildViewHolder){
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
            viewHolder.deleteButton.setOnClickListener(
                    v -> onDeleteButtonListener.doAction(viewHolder.getLayoutPosition())
            );
            viewHolder.itemView.setOnClickListener(
                    v -> onImageClickListener.doAction(viewHolder.getLayoutPosition())
            );
            GlideApp.with(context).load(image).into(viewHolder.image);
        } else {
            holder.itemView.setOnClickListener(onAddButtonClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return images.size() < limit? images.size() + 1: limit;
    }


    @Override
    public long getItemId(int position) {
        if (position < images.size()){
            return images.get(position).hashCode();
        } else {
            return 0;
        }
    }

    public ArrayList<Uri> getImagesList() {
        return images;
    }

    public void addImageAndUpdateView(Uri uri){
        int oldSize = images.size();
        if (oldSize + 1 <= limit){
            notifyItemRemoved(oldSize);
        }
        images.add(uri);
        notifyItemInserted(oldSize);
    }

    public void addImagesAndUpdateView(List<Uri> uris){
        int oldSize = images.size();
        int addedSize = uris.size();

        if (oldSize + addedSize <= limit){
            notifyItemRemoved(oldSize);
        }

        images.addAll(uris);
        notifyItemRangeInserted(oldSize, addedSize);
    }

    public void removeImageAndUpdateView(int position){
        int size = images.size();
        images.remove(position);
        notifyItemRemoved(position);
        if (size == limit){
            notifyItemInserted(size - 1);
        }
    }

    public void clearImages() {
        images.clear();
        images = null;
    }

    private final static class ImageChildViewHolder extends RecyclerView.ViewHolder{

        ImageView image;
        TextView title;
        ImageButton deleteButton;

        ImageChildViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.layout_images_adapter_image_child_image);
            title = itemView.findViewById(R.id.layout_images_adapter_image_child_title);
            deleteButton = itemView.findViewById(R.id.action_delete);
        }
    }

    private final static class AddImageViewHolder extends RecyclerView.ViewHolder{

        AddImageViewHolder(View itemView) {
            super(itemView);
        }
    }

    interface ImageAction{
        void doAction(int position);
    }
}
