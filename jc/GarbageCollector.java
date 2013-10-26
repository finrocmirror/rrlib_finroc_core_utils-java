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
package org.rrlib.finroc_core_utils.jc;

import org.rrlib.finroc_core_utils.jc.container.ConcurrentQueue;
import org.rrlib.finroc_core_utils.jc.log.LogDefinitions;
import org.rrlib.finroc_core_utils.jc.thread.LoopThread;
import org.rrlib.finroc_core_utils.jc.thread.ThreadUtil;
import org.rrlib.finroc_core_utils.log.LogDomain;
import org.rrlib.finroc_core_utils.log.LogLevel;

/**
 * @author Max Reichardt
 *
 * This class/thread deletes objects passed to it - deferred, after a safety period.
 *
 * So it is not a garbage collector in the common sense.
 * It's very minimal/lightweight.
 *
 * Passing objects to this class is non-blocking, so even real-time threads should
 * be able to initiate object deletion.
 *
 * The thread won't have anything to do in "normal" operation of the system...
 * only when objects need to be deleted "safely" during concurrent operation.
 *
 * Normally, it is not required in Java - however, at some place it is used
 * to indicate that resources can safely be reused.
 *
 * Typically, framework elements are prepared for removal and enqueued
 * in the garbage collector's deletion task list.
 * After a certain period of time (when no other thread accesses it
 * anymore), it is completely deleted by this thread.
 *
 * Thread may only be stopped by Thread::stopThreads() in C++.
 */
public class GarbageCollector extends LoopThread {

    /** Current tasks of Garbage Collector */
    private final ConcurrentQueue<DeferredDeleteTask> tasks = new ConcurrentQueue<DeferredDeleteTask>();

    /** Singleton instance - non-null while thread is running */
    private volatile static GarbageCollector instance; /*= new GarbageCollector();*/

    /** Constants for below - weird numbers to detect any memory corruption (shouldn't happen I think) */
    private static final int YES = 0x37347377, NO = 0x1946357;

    /** True after garbage collector has been started */
    private static volatile int started = NO;

    /** Thread id of thread that is deleting garbage collector at program shutdown using Thread::stopThreads() */
    private static volatile long deleterThreadId = 0;

    /** Interval after which all threads should have executed enough code to not access deleted objects anymore - in ms*/
    private static final int SAFE_DELETE_INTERVAL = 5000;

    /** Next delete task - never null */
    private DeferredDeleteTask next = new DeferredDeleteTask();

    /** Log domain for this class */
    public static final LogDomain logDomain = LogDefinitions.finrocUtil.getSubDomain("garbage_collector");

    private GarbageCollector() {
        super(1000, false, false);
        assert(started == NO) : "may only create single instance";
        instance = this;
        setName("Garbage Collector");

        //JavaOnlyBlock
        setDaemon(true);

    }

    /*Cpp
    virtual ~GarbageCollector() {
        assert(tasks.isEmpty());
    }
    */

    public void run() {
        super.run();

        //Cpp assert(Thread::stoppingThreads());

        // delete everything - other threads should have been stopped before
        if (next.elementToDelete != null) {
            next.execute();
        }

        while (!tasks.isEmpty()) {
            tasks.dequeue().execute();
        }
    }

    public void stopThread() {
        //Cpp assert(Thread::stoppingThreads() && "may only be called by Thread::stopThreads()");
        deleterThreadId = ThreadUtil.getCurrentThreadId();

        super.stopThread();
        try {
            join();
        } catch (Exception e) {
            logDomain.log(LogLevel.DEBUG_WARNING, getLogDescription(), e);
        }

        // possibly some thread-local objects of Garbage Collector thread
        while (!tasks.isEmpty()) {
            tasks.dequeue().execute();
        }

        instance = null;
    }

    /**
     * Creates and starts single instance of GarbageCollector thread
     */
    public static void createAndStartInstance() {
        if (started == NO) {
            GarbageCollector tmp = ThreadUtil.getThreadSharedPtr(new GarbageCollector());
            tmp.start();
            instance = tmp;
            started = YES;
        } else {
        }
    }

    /**
     * @param t Thread
     * @return Is this the garbage collector?
     */
    public static boolean isGC(Thread t) {
        return t != null && (t instanceof GarbageCollector);
    }

    public void mainLoopCallback() throws Exception {
        long time = Time.getCoarse();

        // check waiting deletion tasks
        while ((!tasks.isEmpty()) || (next.elementToDelete != null)) {
            if (next.elementToDelete == null) {
                next = tasks.dequeue();
            }

            if (time < next.timeWhen) {
                break;
            }
            next.execute();
        }

        //TODO not necessary
        // cleanup inactive threads
        //ThreadLocalInfo.cleanupThreads();
    }

    /**
     * Delete this object deferred
     * (since this can be called by real-time threads, it must not block! - expect for program shutdown)
     *
     * @param elementToDelete Pointer to object that will be deleted
     */
    public static void deleteDeferred(Object elementToDelete) {
        deleteDeferredImpl(elementToDelete);
    }

    /**
     * Delete this object deferred (implementation)
     * (since this can be called by real-time threads, it must not block! - expect for program shutdown)
     *
     * @param elementToDelete Pointer to object that will be deleted
     */
    private static void deleteDeferredImpl(Object elementToDelete) {

        GarbageCollector gc = instance;
        if (gc == null) {
            assert(started == NO || ThreadUtil.getCurrentThreadId() == deleterThreadId);
            // safe to delete object now
            return;
        }

        DeferredDeleteTask t = new DeferredDeleteTask(elementToDelete, Time.getCoarse() + SAFE_DELETE_INTERVAL);
        gc.tasks.enqueue(t);
    }

    /**
     * Garbage Collector task
     */
    private static class DeferredDeleteTask {

        /** Element to delete */
        private Object elementToDelete;

        /** When to delete element - timestamp in ms */
        private long timeWhen;

        public DeferredDeleteTask() {}

        public DeferredDeleteTask(Object elementToDelete_, long timeWhen_) {
            elementToDelete = elementToDelete_;
            timeWhen = timeWhen_;
        }

        public void execute() {
            if (elementToDelete instanceof HasDestructor) {
                ((HasDestructor)elementToDelete).delete();
            }
            elementToDelete = null;
        }
    }
}
