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

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.github.moko256.latte.client.base.entity.Post
import com.github.moko256.twitlatte.database.CachedIdListSQLiteOpenHelper
import com.github.moko256.twitlatte.entity.Client
import com.github.moko256.twitlatte.entity.EventType
import com.github.moko256.twitlatte.entity.UpdateEvent
import com.github.moko256.twitlatte.glide.GlideApp
import com.github.moko256.twitlatte.model.impl.ListModelImpl
import com.github.moko256.twitlatte.model.impl.StatusActionModelImpl
import com.github.moko256.twitlatte.repository.server.base.ListServerRepository
import com.github.moko256.twitlatte.text.TwitterStringUtils
import com.github.moko256.twitlatte.view.dpToPx
import com.github.moko256.twitlatte.viewmodel.ListViewModel
import com.github.moko256.twitlatte.widget.convertObservableConsumer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

/**
 * Created by moko256 on 2016/03/27.
 *
 * @author moko256
 */
abstract class BaseTweetListFragment : BaseListFragment(), ListServerRepository<Post> {

    protected var adapter: StatusesAdapter? = null
    protected lateinit var client: Client

    private lateinit var disposable: CompositeDisposable
    private lateinit var listViewModel: ListViewModel

    private var adapterObservableBinder: ((UpdateEvent) -> Unit)? = null

    protected abstract val cachedIdsDatabaseName: String

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        val value = TypedValue()
        val toastTopPosition = if (requireContext().theme.resolveAttribute(R.attr.actionBarSize, value, true)) {
            TypedValue.complexToDimensionPixelOffset(value.data, resources.displayMetrics)
        } else {
            0
        }

        client = requireActivity().getClient()!!
        listViewModel = ViewModelProviders.of(this).get(ListViewModel::class.java)
        if (!listViewModel.initialized) {
            listViewModel.listModel = ListModelImpl(
                    this,
                    client,
                    CachedIdListSQLiteOpenHelper(
                            requireContext().applicationContext,
                            client.accessToken,
                            cachedIdsDatabaseName
                    )
            )
            listViewModel.statusActionModel = StatusActionModelImpl(
                    client.apiClient,
                    client.statusCache
            )
            listViewModel.start()
        }

        val dp4 = dpToPx(4)

        recyclerView.clipToPadding = false
        recyclerView.setPadding(dp4, 0, dp4, 0)
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                if (parent.getChildAdapterPosition(view) == 0) {
                    outRect.top = dp4 * 2 - view.paddingTop
                } else {
                    outRect.top = dp4 - view.paddingTop
                }
                outRect.bottom = dp4 - view.paddingBottom
                outRect.left = dp4 - view.paddingLeft
                outRect.right = dp4 - view.paddingRight
            }
        })

        if (activity is GetRecyclerViewPool) {
            recyclerView.setRecycledViewPool((activity as GetRecyclerViewPool).tweetListViewPool)
        }

        adapter = StatusesAdapter(
                client,
                listViewModel.statusActionModel,
                preferenceRepository,
                context,
                listViewModel.listModel.getIdsList(),
                GlideApp.with(this)
        ).also {
            it.setOnLoadMoreClick { position -> listViewModel.listModel.loadOnGap(position) }
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
                        .subscribe {
                            if (it.type == EventType.ADD_TOP && it.size > 0) {
                                Toast.makeText(context, R.string.new_posts, Toast.LENGTH_SHORT).apply {
                                    setGravity(
                                            Gravity.TOP or Gravity.CENTER,
                                            0,
                                            toastTopPosition
                                    )
                                }.show()
                            }
                        },

                listViewModel
                        .listModel
                        .getListEventObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            adapterObservableBinder!!.invoke(it)

                            if (swipeRefreshLayout.isRefreshing) {
                                setRefreshing(false)
                            }
                        },

                listViewModel
                        .listModel
                        .getErrorEventObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            notifyErrorBySnackBar(it).show()

                            if (swipeRefreshLayout.isRefreshing) {
                                setRefreshing(false)
                            }
                        },

                listViewModel.statusActionModel.getStatusObservable().subscribe {
                    adapter!!.notifyItemChanged(listViewModel.listModel.getIdsList().indexOf(it))
                },

                listViewModel.statusActionModel.getDidActionObservable().subscribe {
                    notifyBySnackBar(
                            TwitterStringUtils.getDidActionStringRes(
                                    client.accessToken.clientType,
                                    it
                            )
                    ).show()
                },

                listViewModel.statusActionModel.getErrorObservable().subscribe {
                    it.printStackTrace()
                    notifyErrorBySnackBar(it).show()
                }

        )

        super.onActivityCreated(savedInstanceState)
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

        //Calculated as:
        // Picture area: (16 : 9) + Other content: (16 : 3)
        // ((size.x * 12) / (size.y * 16)) + 1
        val count = (3 * size.x / size.y / 4) + 1

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

    internal interface GetRecyclerViewPool {
        val tweetListViewPool: RecyclerView.RecycledViewPool
    }

}