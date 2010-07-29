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

import org.finroc.jc.annotation.AtFront;
import org.finroc.jc.annotation.CppPrepend;
import org.finroc.jc.annotation.CppType;
import org.finroc.jc.annotation.InCpp;
import org.finroc.jc.annotation.InCppFile;
import org.finroc.jc.annotation.Init;
import org.finroc.jc.annotation.JavaOnly;
import org.finroc.jc.annotation.PassByValue;
import org.finroc.jc.annotation.Ptr;
import org.finroc.jc.annotation.SharedPtr;
import org.finroc.jc.container.ConcurrentQueue;
import org.finroc.jc.log.LogDefinitions;
import org.finroc.jc.thread.LoopThread;
import org.finroc.jc.thread.ThreadUtil;
import org.finroc.log.LogDomain;
import org.finroc.log.LogLevel;

/**
 * @author max
 *
 * Unlike the name might suggest, this is not a garbage collector
 * in the common sense - it's very minimal/lightweight.
 *
 * It won't have anything to do in "normal" operation of the system...
 * only when objects need to be deleted "safely" during concurrent operation.
 *
 * Actually, this class provides a thread that deletes Objects that
 * are passed to it - after a certain period of time.
 *
 * Normally, it is not required in Java - however, at some place it is used
 * to indicate that resources can safely be reused.
 *
 * Typically, framework elements are prepared for removal and enqueued
 * in the garbage collector's deletion task list.
 * After a certain period of time (when no other thread accesses it
 * anymore), it is completely deleted by this thread.
 */
@Ptr
@CppPrepend("std::tr1::shared_ptr<Mutex> GarbageCollector::mutex(new Mutex());")
public class GarbageCollector extends LoopThread {

    /*Cpp
    // Some mutex variables - to keep mutex constructed as long as possible
    // Actual mutex
    static std::tr1::shared_ptr<Mutex> mutex;

    // Copy of mutex in object
    std::tr1::shared_ptr<Mutex> mutexLock;
     */

    /** Current tasks of Garbage Collector */
    private final ConcurrentQueue<DeferredDeleteTask> tasks = new ConcurrentQueue<DeferredDeleteTask>();

    /** Singleton instance - shared pointer so that it is cleanly deleted */
    private static @SharedPtr GarbageCollector instance; /*= new GarbageCollector();*/

    /** Constants for below - weird numbers to detect any memory reuse (shouldn't happen I think) */
    private static final int YES = 0x37347377, NO = 0x1946357;

    /** Has garbage collector already been deleted? - at program end */
    private static int deleted = NO;

    /** Interval after which all threads should have executed enough code to not access deleted objects anymore - in ms*/
    private static final int SAFE_DELETE_INTERVAL = 5000;

    /** Next delete task - never null */
    private DeferredDeleteTask next = new DeferredDeleteTask();

    /** Log domain for this class */
    @InCpp("_RRLIB_LOG_CREATE_NAMED_DOMAIN(logDomain, \"garbage_collector\");")
    private static final LogDomain logDomain = LogDefinitions.finrocUtil.getSubDomain("garbage_collector");

    @Init("mutexLock(mutex)")
    private GarbageCollector() {
        super(1000, false, false);
        setName("Garbage Collector");

        //JavaOnlyBlock
        setDaemon(true);
    }

    /*Cpp
    static void deleteGarbageCollector() {
        deleted = true;
        instance = std::tr1::shared_ptr<GarbageCollector>();
    }

    virtual ~GarbageCollector() { // delete everything - other threads should have been stopped before

        if (next.elementToDelete != NULL) {
            next.execute();
        }

        while(!tasks.isEmpty()) {
            tasks.dequeue().execute();
        }
    }
    */

    public void stopThread() {
        synchronized (this) {
            deleted = YES;
        }

        super.stopThread();
        try {
            join();
        } catch (Exception e) {
            logDomain.log(LogLevel.LL_DEBUG_WARNING, getLogDescription(), e);
        }
        instance = null;
    }

    /**
     * @return Garbage Collector Instance
     */
    private static @Ptr GarbageCollector getInstance() {
        if (instance == null) {
            /*Cpp
            if (Thread::stoppingThreads()) {
                return NULL;
            }
             */

            instance = ThreadUtil.getThreadSharedPtr(new GarbageCollector()); // lazy/safe initialization
            instance.start();
        }
        return instance;
    }

    /**
     * @param t Thread
     * @return Is this the garbage collector?
     */
    public static boolean isGC(@SharedPtr Thread t) {
        return (deleted == NO && t == instance);
    }

    /*Cpp

    // Delete object deferred
    static void deleteDeferred(SafeDestructible* elementToDelete) {
        deleteDeferredImpl(elementToDelete);
    }

    // Delete object deferred
    // (Helper for when SafeDestructible is ambiguous)
    static void deleteDeferred(Object* elementToDelete) {
        deleteDeferredImpl(static_cast<SafeDestructible*>(elementToDelete));
    }

    static const char* getLogDescription() {
        return "GarbageCollector";
    }

    */

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
     *
     * @param elementToDelete Pointer to object that will be deleted
     */
    @JavaOnly
    public synchronized static void deleteDeferred(@CppType("SafeDestructible") @Ptr Object elementToDelete) {
        deleteDeferredImpl(elementToDelete);
    }


    /**
     * Delete this object deferred (implementation)
     *
     * @param elementToDelete Pointer to object that will be deleted
     */
    @InCppFile
    private static void deleteDeferredImpl(@CppType("SafeDestructible") @Ptr Object elementToDelete) {

        /*Cpp
        //static Mutex mutex;

        String s("");
        finroc::util::Object* obj = dynamic_cast<finroc::util::Object*>(elementToDelete);
        if (obj != NULL) {
          s = obj->toString();
        }
        //_printf("Delete requested for: %p %s\n", elementToDelete, s.toCString());
        _FINROC_LOG_STREAM(rrlib::logging::eLL_DEBUG_VERBOSE_1, logDomain, "Delete requested for: ", elementToDelete, " ", s.toCString());
         */

        assert(deleted == YES || deleted == NO);
        GarbageCollector gb = GarbageCollector.getInstance();
        if (deleted == YES || gb == null) {
            // program is shutting down - safe to delete object now
            //Cpp delete elementToDelete;
            return;
        }

        /*Cpp
        Lock l(*mutex);
        */

        if (deleted == NO) {
            DeferredDeleteTask t = new DeferredDeleteTask(elementToDelete, Time.getCoarse() + SAFE_DELETE_INTERVAL);
            if (gb != null) {
                gb.tasks.enqueue(t);
            } else {
                // program is shutting down - safe to delete object now - again... for thread-safety
                //Cpp delete elementToDelete;
            }
        } else {
            // program is shutting down - safe to delete object now - again... for thread-safety
            //Cpp delete elementToDelete;
        }
    }

    /**
     * Garbage Collector task
     */
    @PassByValue @AtFront
    private static class DeferredDeleteTask {

        /*Cpp
        friend class GarbageCollector;
         */

        /** Element to delete */
        private @CppType("SafeDestructible") @Ptr Object elementToDelete;

        /** When to delete element - timestamp in ms */
        private long timeWhen;

        public DeferredDeleteTask() {}

        public DeferredDeleteTask(@CppType("SafeDestructible") @Ptr Object elementToDelete_, long timeWhen_) {
            elementToDelete = elementToDelete_;
            timeWhen = timeWhen_;
        }

        public void execute() {

            // JavaOnlyBlock
            if (elementToDelete instanceof HasDestructor) {
                ((HasDestructor)elementToDelete).delete();
            }

            /*Cpp
            delete elementToDelete;
             */

            elementToDelete = null;
        }
    }

    /*Cpp
    // Functor for use in shared_ptr - deletes objects deferred (via GarbageCollector)
    class Functor {
    public:

        void operator()(SafeDestructible* elementToDelete) {
            //printf("invoking GarbageCollector Functor for %p\n", elementToDelete);
            _FINROC_LOG_STREAM(rrlib::logging::eLL_DEBUG_VERBOSE_1, logDomain, "invoking GarbageCollector Functor for ", elementToDelete);
            GarbageCollector::deleteDeferred(elementToDelete);
        }

        void operator()(Object* elementToDelete) {
            //printf("invoking GarbageCollector Functor for %p\n", elementToDelete);
            _FINROC_LOG_STREAM(rrlib::logging::eLL_DEBUG_VERBOSE_1, logDomain, "invoking GarbageCollector Functor for ", elementToDelete);
            GarbageCollector::deleteDeferred(elementToDelete);
        }

        const char* getLogDescription() {
            return "GarbageCollector::Functor";
        }
    };
    */
}
