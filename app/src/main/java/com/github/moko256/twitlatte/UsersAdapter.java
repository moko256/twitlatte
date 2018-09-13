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

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.github.moko256.twitlatte.entity.Emoji;
import com.github.moko256.twitlatte.entity.User;
import com.github.moko256.twitlatte.glide.GlideApp;
import com.github.moko256.twitlatte.glide.GlideRequests;
import com.github.moko256.twitlatte.text.TwitterStringUtils;
import com.github.moko256.twitlatte.view.EmojiToTextViewSetter;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by moko256 on 2016/03/29.
 *
 * @author moko256
 */
class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    private final List<Long> data;
    private final Context context;

    UsersAdapter(Context context, List<Long> data) {
        this.context = context;
        this.data = data;

        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return data.get(position);
    }

    @NonNull
    @Override
    public UsersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_user, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        User item=GlobalApplication.userCache.get(data.get(i));

        viewHolder.request
                .load(item.get400x400ProfileImageURLHttps())
                .circleCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(viewHolder.userUserImage);

        CharSequence userNameText = TwitterStringUtils.plusUserMarks(
                item.getName(),
                viewHolder.userUserName,
                item.isProtected(),
                item.isVerified()
        );

        viewHolder.userUserName.setText(userNameText);
        Emoji[] userNameEmojis = item.getEmojis();
        if (userNameEmojis != null) {
            if (viewHolder.userNameEmojiSetter == null) {
                viewHolder.userNameEmojiSetter = new EmojiToTextViewSetter(viewHolder.request, viewHolder.userUserName);
            }
            Disposable[] set = viewHolder.userNameEmojiSetter.set(userNameText, userNameEmojis);
            if (set != null) {
                viewHolder.disposable.addAll(set);
            }
        }

        viewHolder.userUserId.setText(TwitterStringUtils.plusAtMark(item.getScreenName()));
        viewHolder.itemView.setOnClickListener(
                v -> {
                    ActivityOptionsCompat animation = ActivityOptionsCompat
                            .makeSceneTransitionAnimation(
                                    ((Activity) context),
                                    viewHolder.userUserImage,
                                    "icon_image"
                            );
                    context.startActivity(
                            ShowUserActivity.getIntent(context, item.getId()),
                            animation.toBundle()
                    );
                }
        );

    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        holder.request.clear(holder.userUserImage);
        holder.disposable.clear();
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

        final ImageView userUserImage;
        final TextView userUserName;
        final TextView userUserId;

        final GlideRequests request;
        final CompositeDisposable disposable;

        EmojiToTextViewSetter userNameEmojiSetter;

        ViewHolder(final View itemView) {
            super(itemView);
            userUserImage = itemView.findViewById(R.id.user_user_image);
            userUserId = itemView.findViewById(R.id.user_user_id);
            userUserName = itemView.findViewById(R.id.user_user_name);
            request = GlideApp.with(itemView);
            disposable = new CompositeDisposable();
        }
    }
}

