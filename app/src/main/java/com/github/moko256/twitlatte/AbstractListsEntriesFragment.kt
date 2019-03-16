/*
 * Copyright 2015-2019 The twitlatte authors
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

package com.github.moko256.twitlatte

import android.os.Bundle
import com.github.moko256.latte.client.base.entity.ListEntry
import com.github.moko256.twitlatte.database.CachedListEntriesSQLiteOpenHelper
import com.github.moko256.twitlatte.entity.Client
import com.github.moko256.twitlatte.rx.LifecycleDisposableContainer
import com.github.moko256.twitlatte.widget.MaterialListTopMarginDecoration
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by moko256 on 2019/01/02.
 *
 * @author moko256
 */
abstract class AbstractListsEntriesFragment: BaseListFragment(), ToolbarTitleInterface {

    private var userId = -1L

    private lateinit var adapter: ListsEntriesAdapter
    private var list = ArrayList<ListEntry>(20)

    private lateinit var disposable: LifecycleDisposableContainer

    private lateinit var client: Client
    private lateinit var helper: CachedListEntriesSQLiteOpenHelper

    override val titleResourceId = R.string.lists

    override fun onCreate(savedInstanceState: Bundle?) {
        if (userId == -1L) {
            userId = arguments!!.getLong("listId", -1L)
        }

        super.onCreate(savedInstanceState)

        disposable = LifecycleDisposableContainer(this)
        client = requireActivity().getClient()!!
        helper = CachedListEntriesSQLiteOpenHelper(
                requireContext().applicationContext,
                client.accessToken,
                userId
        )
        val listEntries = helper.getListEntries()
        if (listEntries.isNotEmpty()) {
            list.addAll(listEntries)
            setRefreshing(false)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        recyclerView.addItemDecoration(MaterialListTopMarginDecoration(resources))

        adapter = ListsEntriesAdapter(requireContext(), list)
        disposable.add(
                adapter.onClickObservable.subscribe {
                    onClickList(it)
                }
        )
        recyclerView.adapter = adapter

        if (!isInitializedList) {
            adapter.notifyDataSetChanged()
        }

    }

    override fun onDestroyView() {
        recyclerView.swapAdapter(null, true)
        super.onDestroyView()
    }

    override fun onDestroy() {
        helper.close()
        super.onDestroy()
    }

    override fun onInitializeList() {
        setRefreshing(true)
        disposable.add(
                getResponseSingle()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { result ->
                                    list.clear()
                                    list.addAll(result)
                                    adapter.notifyDataSetChanged()
                                    setRefreshing(false)
                                },
                                { e ->
                                    e.printStackTrace()
                                    notifyErrorBySnackBar(e).show()
                                    setRefreshing(false)
                                }
                        )
        )
    }

    private fun getResponseSingle(): Single<List<ListEntry>> {
        return Single.create { subscriber ->
            try {
                val listEntries = client.apiClient.getLists(userId)

                helper.setListEntries(listEntries)

                subscriber.onSuccess(listEntries)
            } catch (e: Throwable) {
                subscriber.tryOnError(e)
            }
        }
    }

    override fun onUpdateList() {
        onInitializeList()
    }

    override fun onLoadMoreList() {}

    override fun isInitializedList(): Boolean {
        return list.isNotEmpty()
    }

    abstract fun onClickList(listEntry: ListEntry)
}