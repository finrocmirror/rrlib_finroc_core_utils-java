/**
 * You received this file as part of an advanced experimental
 * robotics framework prototype ('finroc')
 *
 * Copyright (C) 2007-2010 Max Reichardt,
 *   Robotics Research Lab, University of Kaiserslautern
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.finroc.jc;

import org.finroc.jc.annotation.Const;
import org.finroc.jc.annotation.InCpp;
import org.finroc.jc.annotation.Include;
import org.finroc.jc.annotation.Inline;
import org.finroc.jc.annotation.NoCpp;
import org.finroc.jc.annotation.Ptr;
import org.finroc.jc.annotation.Ref;
import org.finroc.jc.annotation.SharedPtr;
import org.finroc.jc.thread.LoopThread;
import org.finroc.log.LogLevel;

/**
 * @author max
 *
 * This class wraps timer access.
 * Getting system time is very expensive (~700k possible/second). If the precise time is not needed
 * this class caches the current time (if enabled). Using this can greatly increase performance
 * (factor 100)
 */
@Inline @NoCpp @Ptr
@Include("<sys/time.h>")
public class Time extends LoopThread {

    /** Singleton instance - shared ptr for auto-deletion */
    private static @SharedPtr Time instance; /* = new Time();*/

    /** Interval in which time is updated */
    private static final int INTERVAL = 20;

    /** Half of above interval */
    private static final int HALF_INTERVAL = INTERVAL / 2;

    /** Run extra thread for timer access? - false will disable most of this classes possible benefits */
    private static final boolean RUN_TIMING_THREAD = true;

    /** Is thread running? */
    private static boolean threadRunning = false;

    /** Nano-seconds per second */
    public static final long NSEC_PER_SEC = 1000000000;

    private Time() {
        super(INTERVAL, true, false);
        setPriority(MAX_PRIORITY - 1);
        setName("Time-Caching-Thread");

        //JavaOnlyBlock
        setDaemon(true);
    }

    public void stopThread() {
        logDomain.log(LogLevel.LL_DEBUG_VERBOSE_1, getLogDescription(), "Stopping time caching thread");
        super.stopThread();
        threadRunning = false;
    }

    /**
     * @return Time loop thread Instance (probably useless method)
     */
    public static @Const @Ref Time getInstance() {
        if (RUN_TIMING_THREAD && instance == null) {
            instance = new Time(); // lazy/safe initialization
            instance.start();
            threadRunning = true;
        }
        return instance;
    }

    /** Current Time - updated periodically by this class */
    private static volatile long curTime = System.currentTimeMillis();

    @Override
    public void mainLoopCallback() throws Exception {
        curTime = getPrecise() + HALF_INTERVAL;
    }

    /**
     * @return Current Time in ms - not precise - updated periodically by this class with specified interval
     */
    public static long getCoarse() {
        return threadRunning ? curTime : getPrecise();
    }

    /**
     * @return Precise time in ms
     */
    @InCpp( {"timeval tval;",
             "_gettimeofday(&tval, NULL);",
             "int64 sec = tval.tv_sec, usec = tval.tv_usec;",
             "return (sec * 1000) + (usec / 1000);"
            })
    public static long getPrecise() {
        return System.currentTimeMillis();
    }

    /**
     * @return Current time in nanoseconds of most precise system clock.
     * Value is relative to some arbitrary point in time -
     * and therefore only suitable for calculating time differences
     * (only valid on local system).
     * (see Java: System.nanoTime)
     */
    @InCpp( {"struct timespec t;",
             "clock_gettime(_CLOCK_MONOTONIC, &t);",
             "int64 sec = t.tv_sec, nsec = t.tv_nsec;",
             "return (sec * NSEC_PER_SEC) + nsec;"
            })
    public static long nanoTime() {
        return System.nanoTime();
    }

    /**
     * Sleep until specified point in time
     *
     * @param ms Point in time in ms (same scale as getCoarse() and getPrecise())
     */
    public static void sleepUntil(long ms) throws InterruptedException {
        long diff = ms - getPrecise();
        if (diff > 0) {
            Thread.sleep(diff);
        }
    }

    /**
     * Sleep until specified point in time
     *
     * @param nano Point in time in nano-seconds (same scale as nanoTime())
     */
    @InCpp( {"struct timespec t;",
             "t.tv_sec = static_cast<int>(nanoTime / NSEC_PER_SEC);",
             "t.tv_nsec = static_cast<int>(nanoTime % NSEC_PER_SEC);",
             "clock_nanosleep(_CLOCK_MONOTONIC, _TIMER_ABSTIME, &t, NULL);"
            })
    public static void sleepUntilNano(long nanoTime) {
        long diff = nanoTime - nanoTime();
        if (diff > 0) {
            long ms = diff / 1000000;
            int nanos = (int)diff % 1000000;
            try {
                Thread.sleep(ms, nanos);
            } catch (InterruptedException e) {
                logDomain.log(LogLevel.LL_DEBUG_WARNING, "", e);
            }
        }
    }
}
