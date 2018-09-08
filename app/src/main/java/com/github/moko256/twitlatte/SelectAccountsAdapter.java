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
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.github.moko256.twitlatte.entity.AccessToken;
import com.github.moko256.twitlatte.entity.User;
import com.github.moko256.twitlatte.glide.GlideApp;
import com.github.moko256.twitlatte.text.TwitterStringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by moko256 on 2017/10/26.
 *
 * @author moko256
 */

public class SelectAccountsAdapter extends RecyclerView.Adapter<SelectAccountsAdapter.ViewHolder> {

    private static final int VIEW_TYPE_IMAGE = 0;
    private static final int VIEW_TYPE_IMAGE_SELECTED = 1;
    private static final int VIEW_TYPE_ADD = 2;
    private static final int VIEW_TYPE_REMOVE = 3;

    private final Context context;

    private final ArrayList<User> users = new ArrayList<>();
    private final ArrayList<AccessToken> accessTokens = new ArrayList<>();

    public OnImageClickListener onImageButtonClickListener;
    public View.OnClickListener onAddButtonClickListener;
    public View.OnClickListener onRemoveButtonClickListener;

    private int selectionPosition = -1;
    private final int selectionColor;

    public SelectAccountsAdapter(Context context){
        this.context = context;

        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorControlHighlight, typedValue, true);
        selectionColor = typedValue.resourceId;
    }

    @Override
    public int getItemViewType(int position) {
        return (position < accessTokens.size())?
                (position == selectionPosition)?
                        VIEW_TYPE_IMAGE_SELECTED:
                        VIEW_TYPE_IMAGE:
                (position == accessTokens.size())?
                        VIEW_TYPE_ADD:
                        VIEW_TYPE_REMOVE;
    }

    @NonNull
    @Override
    public SelectAccountsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == VIEW_TYPE_IMAGE || viewType == VIEW_TYPE_IMAGE_SELECTED?
                R.layout.layout_select_accounts_image_child:
                R.layout.layout_select_accounts_resource_image;
        return new ViewHolder(LayoutInflater.from(context).inflate(layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SelectAccountsAdapter.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        switch (type) {
            case VIEW_TYPE_IMAGE_SELECTED:
            case VIEW_TYPE_IMAGE: {
                User user = users.get(position);
                AccessToken accessToken = accessTokens.get(position);

                if (user != null) {

                    Uri image = Uri.parse(user.get400x400ProfileImageURLHttps());
                    holder.title.setText(TwitterStringUtils.plusAtMark(user.getScreenName(), accessToken.getUrl()));
                    GlideApp
                            .with(holder.itemView)
                            .load(image)
                            .circleCrop()
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(holder.image);
                    holder.itemView.setOnClickListener(v -> {
                        if (onImageButtonClickListener != null) {
                            onImageButtonClickListener.onClick(accessToken);
                        }
                    });

                } else {

                    holder.title.setText(TwitterStringUtils.plusAtMark(accessToken.getScreenName(), accessToken.getUrl()));
                    holder.itemView.setOnClickListener(v -> {
                        if (onImageButtonClickListener != null) {
                            onImageButtonClickListener.onClick(accessToken);
                        }
                    });

                }

                if (type == VIEW_TYPE_IMAGE_SELECTED) {
                    holder.itemView.setBackgroundResource(selectionColor);
                } else {
                    holder.itemView.setBackground(null);
                }
                break;
            }
            case VIEW_TYPE_ADD: {
                holder.image.setImageResource(R.drawable.ic_add_white_24dp);
                holder.title.setText(R.string.login_with_another_account);
                holder.itemView.setOnClickListener(onAddButtonClickListener);
                break;
            }
            case VIEW_TYPE_REMOVE: {
                holder.image.setImageResource(R.drawable.ic_remove_black_24dp);
                holder.title.setText(R.string.do_logout);
                holder.itemView.setOnClickListener(onRemoveButtonClickListener);
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return accessTokens.size() + 2;
    }

    public void clearImages() {
        users.clear();
        accessTokens.clear();
    }

    public void addAndUpdate(List<User> userList, List<AccessToken> accessTokenList){
        users.addAll(userList);
        accessTokens.addAll(accessTokenList);
        notifyDataSetChanged();
    }

    public void setSelectedPosition(AccessToken key){
        selectionPosition = accessTokens.indexOf(key);
    }

    public void updateSelectedPosition(AccessToken key){
        int old = selectionPosition;
        selectionPosition = accessTokens.indexOf(key);
        notifyItemChanged(old);
        notifyItemChanged(selectionPosition);
    }

    public void removeAccessTokensAndUpdate(AccessToken accessToken){
        int position = accessTokens.indexOf(accessToken);
        users.remove(position);
        accessTokens.remove(position);
        notifyItemRemoved(position);
    }

    final static class ViewHolder extends RecyclerView.ViewHolder{
        final ImageView image;
        final TextView title;

        ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.layout_images_adapter_image);
            title = itemView.findViewById(R.id.layout_images_adapter_title);
        }
    }

    public interface OnImageClickListener {
        void onClick(AccessToken accessToken);
    }
}
