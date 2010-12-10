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
package org.finroc.jc.thread;

import org.finroc.jc.annotation.Const;
import org.finroc.jc.annotation.Include;
import org.finroc.jc.annotation.JavaOnly;
import org.finroc.jc.annotation.SharedPtr;
import org.finroc.jc.annotation.Virtual;
import org.finroc.jc.log.LogDefinitions;
import org.finroc.log.LogDomain;
import org.finroc.log.LogLevel;

/**
 * @author max
 *
 * A Thread that calls a callback function with a specified rate
 */
@Include("Thread.h")
public abstract class LoopThread extends Thread {

    /** Signals for state change */
    @JavaOnly // already in thread base class
    private volatile boolean stopSignal = false;
    private volatile boolean pauseSignal = false;

    /** Cycle time with which callback function is called */
    private long cycleTime;

    /** Display warning, if cycle time is exceeded? */
    @Const private final boolean warnOnCycleTimeExceed;

    /** Display warnings on console? */
    private static final boolean DISPLAYWARNINGS = false;

    /**
     * Is Thread currently waiting?
     * More precisely: Is thread currently waiting or executing uncritical code in waitFor-method?
     */
    private volatile boolean waiting;

    /** Log domain for this class */
    @JavaOnly
    public static final LogDomain logDomain = LogDefinitions.finrocUtil.getSubDomain("thread");

    /**
     * @param defaultCycleTime Cycle time with which callback function is called
     */
    public LoopThread(long defaultCycleTime) {
        this(defaultCycleTime, false, false);
    }

    /**
     * @param defaultCycleTime Cycle time with which callback function is called
     * @param warnOnCycleTimeExceed Display warning, if cycle time is exceeded?
     */
    public LoopThread(long defaultCycleTime, boolean warnOnCycleTimeExceed) {
        this(defaultCycleTime, warnOnCycleTimeExceed, false);
    }

    /**
     * @param defaultCycleTime Cycle time with which callback function is called
     * @param warnOnCycleTimeExceed Display warning, if cycle time is exceeded?
     * @param pauseOnStartup Pause Signal set at startup of this thread?
     */
    public LoopThread(long defaultCycleTime, boolean warnOnCycleTimeExceed, boolean pauseOnStartup) {
        pauseSignal = pauseOnStartup;
        cycleTime = defaultCycleTime;
        this.warnOnCycleTimeExceed = warnOnCycleTimeExceed;
        setName(getClass().getSimpleName() + " MainLoop");
    }

    public void run() {
        try {

            //stopSignal = false; // this may lead to unintended behaviour

            // Start main loop
            mainLoop();

        } catch (InterruptedException ie) {

            //System.out.println(toString() + " Interrupted");
            logDomain.log(LogLevel.LL_DEBUG, getLogDescription(), "Uncaught Thread Interrupt");
        } catch (Exception e) {
            logDomain.log(LogLevel.LL_DEBUG, getLogDescription(), "Uncaught Thread Exception - ", e);
        }
    }

    /**
     * The main loop
     */
    private void mainLoop() throws Exception {

        while (!stopSignal) {

            if (pauseSignal) {
                waitUntilNotification();
                continue;
            }

            // remember start time
            long startTimeMs = System.currentTimeMillis();

            mainLoopCallback();

            // wait
            long waitForX = cycleTime - (System.currentTimeMillis() - startTimeMs);
            if (waitForX < 0 && warnOnCycleTimeExceed && DISPLAYWARNINGS) {
                //System.err.println("warning: Couldn't keep up cycle time (" + (-waitForX) + " ms too long)");
                logDomain.log(LogLevel.LL_WARNING, getLogDescription(), "warning: Couldn't keep up cycle time (" + (-waitForX) + " ms too long)");
            } else if (waitForX > 0) {
                waitFor(waitForX);
            }
        }
    }

    /**
     * Convenient wait method.
     * May only be called by current thread.
     *
     * @param waitFor Period in milliseconds
     */
    public void waitFor(long waitFor) {
        assert getId() == ThreadUtil.getCurrentThreadId();
        try {
            waiting = true;
            Thread.sleep(waitFor);
            waiting = false;
        } catch (InterruptedException e) {
            //System.out.println("wait for " + toString() + " Interrupted");
            logDomain.log(LogLevel.LL_DEBUG, getLogDescription(), "Thread interrupted waiting for next loop");
            waiting = false;
        }
    }

    /**
     * Let thread sleep until it is interrupted.
     * May only be called by current thread.
     */
    public void waitUntilNotification() {
        assert getId() == ThreadUtil.getCurrentThreadId();
        synchronized (this) {
            try {
                waiting = true;
                wait();
                waiting = false;
            } catch (InterruptedException e) {
                //System.out.println("wait for " + toString() + " Interrupted");
                waiting = false;
            }
        }
    }


    /**
     * Callback function that is called with the specified rate
     */
    public abstract void mainLoopCallback() throws Exception;

    /**
     * @return Current Cycle time with which callback function is called
     */
    public long getCycleTime() {
        return cycleTime;
    }

    /**
     * @param cycleTime New Cycle time with which callback function is called
     */
    public void setCycleTime(long cycleTime) {
        this.cycleTime = cycleTime;
    }

    /**
     * @return Is thread currently running? (and not paused)
     */
    public boolean isRunning() {
        return isAlive() && !isPausing();
    }

    /**
     * Stop Loop. Cannot be restarted.
     */
    @Virtual public synchronized void stopThread() {
        if (waiting) {
            notify();
        }
        stopSignal = true;
    }

    /**
     * Stop Loop. Cannot be restarted (same as StopThread)
     */
    public void stopLoop() {
        stopThread();
    }


    /**
     * Pause Thread.
     */
    public void pauseThread() {
        pauseSignal = true;
    }

    /**
     * Pause thread (same as pauseThread())
     */
    public void pauseLoop() {
        pauseThread();
    }

    /**
     * Resume Thread;
     */
    public void continueThread() {
        pauseSignal = false;
        synchronized (this) {
            notify();
        }
    }

    /**
     * Resume Thread (same as continueThread)
     */
    public void continueLoop() {
        continueThread();
    }


    /**
     * @return Is Thread currently paused?
     */
    public boolean isPausing() {
        return pauseSignal;
    }

    /**
     * @return Is the stop signal set in order to stop the thread?
     */
    public boolean isStopSignalSet() {
        return stopSignal;
    }

    /**
     * @return Is Thread currently waiting?
     * More precisely: Is thread currently waiting or executing uncritical code in waitFor-method?
     */
    public boolean isWaiting() {
        return waiting;
    }

    /**
     * @param Object (in shared pointer) that thread shall "lock"
     * (it won't be deleted as long as thread exists)
     *
     * (dummy method in Java - simplifies calling C++ equivalent in Thread)
     */
    @JavaOnly
    public void lockObject(@SharedPtr Object o) {}

    /**
     * @return log description
     */
    @JavaOnly
    public String getLogDescription() {
        return getName();
    }
}
