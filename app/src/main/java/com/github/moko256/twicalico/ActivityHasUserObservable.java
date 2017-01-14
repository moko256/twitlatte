package com.github.moko256.twicalico;

import rx.Observable;
import twitter4j.User;

/**
 * Created by moko256 on 2017/01/15.
 *
 * @author moko256
 */

public interface ActivityHasUserObservable {
    Observable<User> getUserObservable();
}
