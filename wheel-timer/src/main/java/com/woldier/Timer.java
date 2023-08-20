package com.woldier;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * description TODO
 *
 * @author: woldier wong
 * @date: 2023/8/20$ 12:04$
 */
public interface Timer {
    Timeout newTimeout(TimerTask task, long timeout, TimeUnit timeUnit);

    Set<Timeout> stop();
}
