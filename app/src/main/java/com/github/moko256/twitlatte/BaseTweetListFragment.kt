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

package com.github.moko256.twitlatte

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.github.moko256.twitlatte.converter.convertToPost
import com.github.moko256.twitlatte.database.CachedIdListSQLiteOpenHelper
import com.github.moko256.twitlatte.entity.EventType
import com.github.moko256.twitlatte.entity.Post
import com.github.moko256.twitlatte.entity.UpdateEvent
import com.github.moko256.twitlatte.model.impl.ListModelImpl
import com.github.moko256.twitlatte.model.impl.StatusActionModelImpl
import com.github.moko256.twitlatte.repository.server.base.ListServerRepository
import com.github.moko256.twitlatte.repository.server.impl.TwitterStatusActionRepositoryImpl
import com.github.moko256.twitlatte.text.TwitterStringUtils
import com.github.moko256.twitlatte.viewmodel.ListViewModel
import com.github.moko256.twitlatte.widget.convertObservableConsumer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import twitter4j.Paging
import twitter4j.ResponseList
import twitter4j.Status
import twitter4j.TwitterException

/**
 * Created by moko256 on 2016/03/27.
 *
 * @author moko256
 */
abstract class BaseTweetListFragment : BaseListFragment() {

    protected var adapter: StatusesAdapter? = null

    private lateinit var disposable: CompositeDisposable

    private lateinit var listViewModel: ListViewModel

    private var adapterObservableBinder: Function1<UpdateEvent, Unit>? = null

    protected abstract val cachedIdsDatabaseName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        listViewModel = ViewModelProviders.of(this).get(ListViewModel::class.java)
        if (!listViewModel.initilized) {
            listViewModel.listModel = ListModelImpl(
                    object : ListServerRepository<Post> {
                        override fun get(sinceId: Long?, maxId: Long?, limit: Int): List<Post> {
                            val paging = Paging().count(limit)
                            if (sinceId != null) {
                                paging.sinceId = sinceId
                            }
                            if (maxId != null) {
                                paging.maxId = maxId
                            }
                            if (sinceId == null && maxId == null) {
                                paging.page = 1
                            }
                            return getResponseList(paging).map {
                                it.convertToPost()
                            }
                        }
                    },
                    CachedIdListSQLiteOpenHelper(
                            requireContext().applicationContext,
                            GlobalApplication.accessToken,
                            cachedIdsDatabaseName
                    )
            )
            listViewModel.statusActionModel = StatusActionModelImpl(
                    TwitterStatusActionRepositoryImpl(
                            GlobalApplication.twitter
                    ),
                    GlobalApplication.statusActionQueue,
                    GlobalApplication.statusCache
            )
            listViewModel.start()
        }
    }

    @SuppressLint("WrongConstant")
    override fun onActivityCreated(@Nullable savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val dp8 = Math.round(8f * resources.displayMetrics.density)

        recyclerView.setPadding(dp8, 0, 0, 0)
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.right = dp8
                outRect.top = dp8
            }
        })

        if (activity is GetRecyclerViewPool) {
            recyclerView.setRecycledViewPool((activity as GetRecyclerViewPool).tweetListViewPool)
        }

        adapter = StatusesAdapter(context, listViewModel.listModel.getIdsList())
        adapter!!.setOnLoadMoreClick { position -> listViewModel.listModel.loadOnGap(position) }
        adapter!!.setOnFavoriteClick { _, id, hasFavorited ->
            if (hasFavorited) {
                listViewModel.statusActionModel.removeFavorite(id)
            } else {
                listViewModel.statusActionModel.createFavorite(id)
            }
        }
        adapter!!.setOnRepeatClick { _, id, hasRepeated ->
            if (hasRepeated) {
                listViewModel.statusActionModel.removeRepeat(id)
            } else {
                listViewModel.statusActionModel.createRepeat(id)
            }
        }

        recyclerView.adapter = adapter
        if (!isInitializedList) {
            adapter!!.notifyDataSetChanged()
        }

        recyclerView.layoutManager!!.scrollToPosition(listViewModel.listModel.getSeeingPosition())

        adapterObservableBinder = recyclerView.convertObservableConsumer()

        disposable = CompositeDisposable(
                listViewModel
                        .listModel
                        .getListEventObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { it ->
                            if (it.type == EventType.ADD_TOP && it.size > 0) {
                                val value = TypedValue()
                                Toast.makeText(context, R.string.new_posts, Toast.LENGTH_SHORT).apply {
                                    setGravity(
                                            Gravity.TOP or Gravity.CENTER,
                                            0,
                                            if (requireContext().theme.resolveAttribute(R.attr.actionBarSize, value, true)) {
                                                TypedValue.complexToDimensionPixelOffset(value.data, resources.displayMetrics)
                                            } else {
                                                0
                                            }
                                    )
                                }.show()
                            }
                        },

                listViewModel
                        .listModel
                        .getListEventObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { it ->
                            adapterObservableBinder!!.invoke(it)

                            if (swipeRefreshLayout.isRefreshing) {
                                setRefreshing(false)
                            }
                        },

                listViewModel
                        .listModel
                        .getErrorEventObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { it ->
                            notifyErrorBySnackBar(it).show()

                            if (swipeRefreshLayout.isRefreshing) {
                                setRefreshing(false)
                            }
                        },

                listViewModel.statusActionModel.getUpdateObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            adapter!!.notifyItemChanged(listViewModel.listModel.getIdsList().indexOf(it.first))

                            notifyBySnackBar(
                                    TwitterStringUtils.getDidActionStringRes(
                                            GlobalApplication.clientType,
                                            it.second
                                    )
                            ).show()
                        },

                listViewModel.statusActionModel.getErrorObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            it.second.printStackTrace()
                            notifyErrorBySnackBar(it.second).show()
                            adapter!!.notifyItemChanged(listViewModel.listModel.getIdsList().indexOf(it.first))
                        }
        )
    }

    override fun onDestroyView() {
        disposable.dispose()
        adapterObservableBinder = null
        val layoutManager = recyclerView.layoutManager
        val position = getFirstVisibleItemPosition(layoutManager)
        listViewModel.listModel.removeOldCache(position)
        recyclerView.swapAdapter(null, true)
        adapter = null
        super.onDestroyView()
    }

    override fun onStop() {
        super.onStop()
        val position = getFirstVisibleItemPosition(recyclerView.layoutManager)
        if (position >= 0) {
            listViewModel.listModel.updateSeeingPosition(position)
        }
    }

    override fun onInitializeList() {
        setRefreshing(true)
        listViewModel.listModel.refreshFirst()
    }

    override fun onUpdateList() {
        listViewModel.listModel.refreshOnTop()
    }

    override fun onLoadMoreList() {
        listViewModel.listModel.loadOnBottom()
    }

    override fun isInitializedList(): Boolean {
        return !listViewModel.listModel.getIdsList().isEmpty()
    }

    override fun initializeRecyclerViewLayoutManager(): RecyclerView.LayoutManager {
        val wm = requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay

        val size = Point()
        display.getSize(size)

        val count = Math.ceil(
                //Calculated as:
                //Picture area: (16 : 9) + Other content: (16 : 3)
                (size.x * 12).toDouble() / (size.y * 16)
        ).toInt()
        return if (count == 1) {
            val layoutManager = LinearLayoutManager(context)
            layoutManager.recycleChildrenOnDetach = true
            layoutManager
        } else {
            StaggeredGridLayoutManager(
                    count,
                    StaggeredGridLayoutManager.VERTICAL
            )
        }
    }

    @Throws(TwitterException::class)
    protected abstract fun getResponseList(paging: Paging): ResponseList<Status>

    internal interface GetRecyclerViewPool {
        val tweetListViewPool: RecyclerView.RecycledViewPool
    }

}