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

package com.github.moko256.twitlatte.cacheMap

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.moko256.twitlatte.testdata.testPost
import com.github.moko256.twitlatte.testdata.testStatus
import com.github.moko256.twitlatte.testdata.testUser
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PostCacheTest {

    lateinit var statusCacheMap: StatusCacheMap
    lateinit var userCacheMap: UserCacheMap

    lateinit var postCache: PostCache

    @Before
    fun setUp() {
        statusCacheMap = StatusCacheMap()
        userCacheMap = UserCacheMap()
        postCache = PostCache(statusCacheMap, userCacheMap)
    }

    @Test
    fun getUpdatedEntitiesForAddSimple() {
        TestObserver.create<PostCache.UpdatedEntities>().also { testObserver ->
            postCache.updatedEntities.subscribe(testObserver)
            val post = testPost(statusId = 1, userId = 2)
            postCache.add(post, false)
            testObserver.assertValue(PostCache.UpdatedEntities(listOf(testStatus(1)), listOf(testUser(2))))
        }
    }

    @Test
    fun getUpdatedEntitiesForAddComplex() {
        TestObserver.create<PostCache.UpdatedEntities>().also { testObserver ->
            postCache.updatedEntities.subscribe(testObserver)
            val post = testPost(
                    4, 5, 6, 7, 8, 9
            )
            postCache.add(post, false)
            testObserver.assertValue(PostCache.UpdatedEntities(
                    listOf(post.status!!, post.quotedRepeatingStatus!!, post.repeat!!),
                    listOf(post.user!!, post.repeatedUser!!, post.quotedRepeatingUser!!)
            ))
        }
    }

}