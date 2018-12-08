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

import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;

import com.github.moko256.twitlatte.database.CachedTrendsSQLiteOpenHelper;
import com.github.moko256.twitlatte.entity.Client;
import com.github.moko256.latte.client.base.entity.Trend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by moko256 on 2017/07/05.
 *
 * @author moko256
 */

public class TrendsFragment extends BaseListFragment {
    private TrendsAdapter adapter;
    private List<Trend> list;

    private CompositeDisposable disposable;

    private Client client;
    private CachedTrendsSQLiteOpenHelper helper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        disposable = new CompositeDisposable();
        client = GlobalApplicationKt.getClient(requireActivity());
        helper = new CachedTrendsSQLiteOpenHelper(
                requireContext().getApplicationContext(),
                client.getAccessToken()
        );
        List<Trend> trends = helper.getTrends();
        if (trends.size() > 0){
            list = trends;
            setRefreshing(false);
        } else {
            list = new ArrayList<>(20);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                if (parent.getChildAdapterPosition(view)==0){
                    outRect.top=Math.round(getResources().getDisplayMetrics().density*8f);
                }
            }
        });

        adapter=new TrendsAdapter(getContext(), list);
        recyclerView.setAdapter(adapter);
        if (!isInitializedList()){
            adapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onDestroyView() {
        recyclerView.swapAdapter(null, true);
        adapter=null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        disposable.dispose();
        super.onDestroy();
        disposable = null;
        helper.close();
        helper = null;
        list=null;
    }

    @Override
    protected void onInitializeList() {
        setRefreshing(true);
        disposable.add(
                getGeoLocationSingle()
                        .flatMap(this::getResponseSingle)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result-> {
                                    list.clear();
                                    list.addAll(result);
                                    adapter.notifyDataSetChanged();
                                    setRefreshing(false);
                                },
                                e -> {
                                    e.printStackTrace();
                                    notifyErrorBySnackBar(e).show();
                                    setRefreshing(false);
                                }
                        )
        );
    }

    @Override
    protected void onUpdateList() {
        onInitializeList();
    }

    @Override
    protected void onLoadMoreList() {}

    @Override
    protected boolean isInitializedList() {
        return !list.isEmpty();
    }

    @Override
    protected RecyclerView.LayoutManager initializeRecyclerViewLayoutManager() {
        return new LinearLayoutManager(getContext());
    }

    private Single<List<Trend>> getResponseSingle(Address address) {
        return Single.create(
                subscriber->{
                    try {
                        List<Trend> trends = client.getApiClient().getClosestTrends(address.getLatitude(), address.getLongitude());

                        helper.setTrends(trends);

                        subscriber.onSuccess(trends);
                    } catch (Throwable e) {
                        subscriber.tryOnError(e);
                    }
                }
        );
    }

    private Single<Address> getGeoLocationSingle(){
        return Single.create(
                subscriber -> {
                    try {
                        Geocoder geocoder = new Geocoder(getContext());
                        Locale locale = Locale.getDefault();
                        Address address = geocoder.getFromLocationName(locale.getDisplayCountry(), 1).get(0);
                        if (address != null){
                            subscriber.onSuccess(address);
                        } else {
                            subscriber.tryOnError(new Exception("Cannot use trends"));
                        }
                    } catch(IOException e){
                        subscriber.tryOnError(e);
                    }
                }
        );
    }

}