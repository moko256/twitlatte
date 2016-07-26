package com.moko256.twitterviewer256;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Created by moko256 on GitHub on 2016/06/30.
 */
public class ShowUserFragment extends BaseTweetListFragment {

    User user;

    @Override
    public void initializationProcess(View view) {
        Observable
                .create(
                        subscriber->{
                            String userName=(String) getActivity().getIntent().getSerializableExtra("userName");
                            User user=null;
                            if(userName!=null){
                                try {
                                    user=Static.twitter.showUser(userName);
                                } catch (TwitterException e) {
                                    subscriber.onError(e);
                                }
                            }else {
                                user=(User) getActivity().getIntent().getSerializableExtra("user");
                            }

                            subscriber.onNext(user);
                            subscriber.onCompleted();
                        }
                )
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result->{
                            user=(User)result;
                            Glide.with(getContext()).load(user.getBiggerProfileImageURL()).into((ImageView) view.findViewById(R.id.show_user_image));
                            Glide.with(getContext()).load(user.getProfileBannerRetinaURL()).into((ImageView)view.findViewById(R.id.show_user_bgimage));

                            ((TextView)view.findViewById(R.id.show_user_bio)).setText(user.getDescription());
                            //((Toolbar) getActivity().findViewById(R.id.show_user_tool_bar)).setTitle(user.getName());
                            super.initializationProcess(view);

                        },
                        throwable->{
                            throwable.printStackTrace();
                            getActivity().finish();
                        },
                        ()->{}
                );
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putSerializable("user", user);
    }

    @Override
    public ResponseList<Status> getResponseList(Paging paging) throws TwitterException {
        return Static.twitter.getUserTimeline(user.getId(),paging);
    }

    @Override
    public int getLayoutResource() {
        return R.layout.fragment_show_user;
    }
}
