package com.github.moko256.twicalico;

import java.util.Collection;
import java.util.HashMap;

import twitter4j.User;

/**
 * Created by moko256 on 2016/12/22.
 *
 * @author moko256
 */

public class UserCacheMap {

    private HashMap<Long, User> cache=new HashMap<>();

    public int size() {
        return cache.size();
    }

    public boolean isEmpty() {
        return cache.isEmpty();
    }

    public void add(User user) {
        cache.put(user.getId(), user);
    }

    public void addAll(Collection<? extends User> c) {
        for (User user : c) {
            add(user);
        }
    }

    public User get(long id) {
        return cache.get(id);
    }

    public void clear() {

    }
}