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

package com.github.moko256.twitlatte.database;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.moko256.latte.client.base.entity.Trend;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static com.github.moko256.twitlatte.testutils.EmptyAccessTokenKt.emptyAccessToken;
import static org.junit.Assert.assertEquals;

/**
 * Created by moko256 on 2017/07/05.
 *
 * @author moko256
 */
@RunWith(AndroidJUnit4.class)
public class CachedTrendsSQLiteOpenHelperTest {

    private CachedTrendsSQLiteOpenHelper helper = new CachedTrendsSQLiteOpenHelper(
            ApplicationProvider.getApplicationContext(),
            emptyAccessToken
    );

    private String[] input0 = new String[]{"0", "1", "2"};
    private String[] input1 = new String[]{"3", "4"};

    @Test
    public void test() {
        helper.getWritableDatabase().delete("Trends", null, null);

        setTrendsTest();
        updateTrendsTest();
        helper.close();
    }


    private void setTrendsTest() {
        helper.setTrends(createTestTrends(input0));

        List<Trend> result0 = helper.getTrends();
        for (int i = 0; i < result0.size(); i++) {
            assertEquals(result0.get(i).getName(), input0[i]);
        }
    }

    private void updateTrendsTest() {
        helper.setTrends(createTestTrends(input1));
        List<Trend> result1 = helper.getTrends();
        for (int i = 0; i < result1.size(); i++) {
            assertEquals(result1.get(i).getName(), input1[i]);
        }
    }

    private List<Trend> createTestTrends(String[] names) {
        List<Trend> r = new ArrayList<>(names.length);
        for (String name : names) {
            r.add(new Trend(
                    name, -1
            ));
        }
        return r;
    }
}
