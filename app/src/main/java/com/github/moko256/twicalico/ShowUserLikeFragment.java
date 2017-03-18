/*
 * Copyright 2016 The twicalico authors
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

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Created by moko256 on 2017/03/04.
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