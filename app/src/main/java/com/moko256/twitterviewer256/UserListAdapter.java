package com.moko256.twitterviewer256;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import twitter4j.User;

/**
 * Created by moko256 on 2016/03/29.
 *
 * @author moko256
 */
class UserListAdapter extends BaseListAdapter<User,UserListAdapter.ViewHolder> {

    UserListAdapter(Context context, ArrayList<User> data) {
        super(context, data);
    }

    @Override
    public UserListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(inflater.inflate(R.layout.layout_user, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int i) {
        User item=data.get(i);

        Glide.with(context).load(item.getProfileImageURL()).into(viewHolder.userUserImage);

        viewHolder.userUserName.setText(item.getName());
        viewHolder.userUserId.setText(TwitterStringUtil.plusAtMark(item.getScreenName()));
        viewHolder.itemView.setOnClickListener(v -> {
            ViewCompat.setTransitionName(viewHolder.userUserImage,"tweet_user_image");
            Intent intent = new Intent(context,ShowUserActivity.class);
            intent.putExtra("user",item);
            context.startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context,viewHolder.userUserImage,"tweet_user_image").toBundle());
        });

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

        ImageView userUserImage;
        TextView userUserName;
        TextView userUserId;

        ViewHolder(final View itemView) {
            super(itemView);
            userUserImage=(ImageView) itemView.findViewById(R.id.user_user_image);
            userUserId=(TextView) itemView.findViewById(R.id.user_user_id);
            userUserName=(TextView) itemView.findViewById(R.id.user_user_name);
        }
    }
}

