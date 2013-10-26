//
// You received this file as part of RRLib
// Robotics Research Library
//
// Copyright (C) Finroc GbR (finroc.org)
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
//
//----------------------------------------------------------------------
package org.rrlib.finroc_core_utils.jc.thread;

import org.rrlib.finroc_core_utils.jc.log.LogDefinitions;
import org.rrlib.finroc_core_utils.log.LogDomain;
import org.rrlib.finroc_core_utils.log.LogLevel;

/**
 * @author Max Reichardt
 *
 * A Thread that calls a callback function with a specified rate
 */
public abstract class LoopThread extends Thread {

    /** Signals for state change */
    // already in thread base class
    private volatile boolean stopSignal = false;
    private volatile boolean pauseSignal = false;

    /** Cycle time with which callback function is called */
    private long cycleTime;

    /** Display warning, if cycle time is exceeded? */
    private final boolean warnOnCycleTimeExceed;

    /** Display warnings on console? */
    private static final boolean DISPLAYWARNINGS = false;

    /**
     * Is Thread currently waiting?
     * More precisely: Is thread currently waiting or executing uncritical code in waitFor-method?
     */
    private volatile boolean waiting;

    /**
     * Time spent in last call to MainLoopCallback()
     */
    private long lastCycleTime;

    /** Start time of last cycle */
    private long lastCycleStart;

    /** Log domain for this class */
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
            logDomain.log(LogLevel.DEBUG, getLogDescription(), "Uncaught Thread Interrupt");
        } catch (Exception e) {
            logDomain.log(LogLevel.DEBUG, getLogDescription(), "Uncaught Thread Exception - ", e);
        }
    }

    /**
     * The main loop
     */
    private void mainLoop() throws Exception {

        while (!stopSignal) {

            if (pauseSignal) {
                lastCycleStart = 0;
                waitUntilNotification();
                continue;
            }

            if (lastCycleStart != 0) {
                // wait
                lastCycleTime = (System.currentTimeMillis() - lastCycleStart);
                long waitForX = cycleTime - lastCycleTime;
                if (waitForX < 0 && warnOnCycleTimeExceed && DISPLAYWARNINGS) {
                    //System.err.println("warning: Couldn't keep up cycle time (" + (-waitForX) + " ms too long)");
                    logDomain.log(LogLevel.WARNING, getLogDescription(), "warning: Couldn't keep up cycle time (" + (-waitForX) + " ms too long)");
                } else if (waitForX > 0) {
                    waitFor(waitForX);
                }
                lastCycleStart += cycleTime;
                if (waitForX < 0) {
                    lastCycleStart = System.currentTimeMillis();
                }
            } else {
                lastCycleStart = System.currentTimeMillis();
            }

            mainLoopCallback();

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
            if (waitFor <= 100) {
                Thread.sleep(waitFor);
            } else {
                synchronized (this) {
                    waiting = true;
                    wait(waitFor);
                    waiting = false;
                }
            }
        } catch (InterruptedException e) {
            //System.out.println("wait for " + toString() + " Interrupted");
            logDomain.log(LogLevel.DEBUG, getLogDescription(), "Thread interrupted waiting for next loop");
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
    public synchronized void stopThread() {
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
     * @return Is Thread currently waiting? (for more than 100ms on object's condition variable)
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
    public void lockObject(Object o) {}

    /**
     * @return log description
     */
    public String getLogDescription() {
        return getName();
    }

    /**
     * \return Time spent in last call to MainLoopCallback()
     */
    public long getLastCycleTime() {
        return lastCycleTime;
    }
}
