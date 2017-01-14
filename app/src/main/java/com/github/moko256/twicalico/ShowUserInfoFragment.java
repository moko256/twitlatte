package com.github.moko256.twicalico;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import twitter4j.User;

/**
 * Created by moko256 on 2017/01/15.
 *
 * @author moko256
 */

public class ShowUserInfoFragment extends Fragment implements ToolbarTitleInterface {

    RequestManager requestManager;

    ImageView header;
    ImageView icon;

    TextView userNameText;
    TextView userIdText;
    TextView userBioText;
    TextView userTweetsCount;
    TextView userFollowCount;
    TextView userFollowerCount;

    User user;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityHasUserObservable activity=(ActivityHasUserObservable)getActivity();
        activity.getUserObservable()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result->{
                            user=result;
                            setShowUserInfo();
                        },
                        throwable->{
                            throwable.printStackTrace();
                            getActivity().finish();
                        },
                        ()->{}
                );
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_show_user_info,container,false);

        requestManager=Glide.with(this);

        header=(ImageView) view.findViewById(R.id.show_user_bgimage);
        icon=(ImageView) view.findViewById(R.id.show_user_image);

        userNameText=(TextView) view.findViewById(R.id.show_user_name);
        userIdText=(TextView) view.findViewById(R.id.show_user_id);
        userBioText =(TextView) view.findViewById(R.id.show_user_bio);
        userTweetsCount=(TextView) view.findViewById(R.id.show_user_tweets_count);
        userFollowCount=(TextView) view.findViewById(R.id.show_user_follow_count);
        userFollowerCount=(TextView) view.findViewById(R.id.show_user_follower_count);

        return view;
    }

    @Override
    public int getTitleResourceId() {
        return R.string.account;
    }

    private void setShowUserInfo() {
        requestManager.load(user.getProfileBannerRetinaURL()).into(header);
        requestManager.load(user.getBiggerProfileImageURL()).into(icon);

        userNameText.setText(user.getName());
        userIdText.setText(TwitterStringUtil.plusAtMark(user.getScreenName()));
        getActivity().setTitle(user.getName());
        userBioText.setText(user.getDescription());

        userTweetsCount.setText("Tweets: "+String.valueOf(user.getStatusesCount()));
        userFollowCount.setText("Follow: "+String.valueOf(user.getFriendsCount()));
        userFollowerCount.setText("Followers: "+String.valueOf(user.getFollowersCount()));
    }
}
