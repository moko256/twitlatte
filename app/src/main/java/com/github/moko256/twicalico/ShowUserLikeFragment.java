package com.github.moko256.twicalico;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Created by moko256 on 2016/06/30.
 *
 * @author moko256
 */
public class ShowUserLikeFragment extends BaseTweetListFragment implements ToolbarTitleInterface {

    User user;

    @Override
    public void onInitializeList() {
        ActivityHasUserObservable activity=(ActivityHasUserObservable)getActivity();
        activity.getUserObservable()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result->{
                            user=result;
                            super.onInitializeList();
                        },
                        throwable->{
                            throwable.printStackTrace();
                            getActivity().finish();
                        },
                        ()->{}
                );
    }

    @Override
    public ResponseList<Status> getResponseList(Paging paging) throws TwitterException {
        return GlobalApplication.twitter.getFavorites(user.getId(),paging);
    }

    @Override
    public int getTitleResourceId() {
        return R.string.like;
    }
}