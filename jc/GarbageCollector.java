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
package org.rrlib.finroc_core_utils.jc;

import org.rrlib.finroc_core_utils.jc.annotation.AtFront;
import org.rrlib.finroc_core_utils.jc.annotation.CppPrepend;
import org.rrlib.finroc_core_utils.jc.annotation.CppType;
import org.rrlib.finroc_core_utils.jc.annotation.InCpp;
import org.rrlib.finroc_core_utils.jc.annotation.InCppFile;
import org.rrlib.finroc_core_utils.jc.annotation.JavaOnly;
import org.rrlib.finroc_core_utils.jc.annotation.PassByValue;
import org.rrlib.finroc_core_utils.jc.annotation.Ptr;
import org.rrlib.finroc_core_utils.jc.annotation.SharedPtr;
import org.rrlib.finroc_core_utils.jc.container.ConcurrentQueue;
import org.rrlib.finroc_core_utils.jc.log.LogDefinitions;
import org.rrlib.finroc_core_utils.jc.thread.LoopThread;
import org.rrlib.finroc_core_utils.jc.thread.ThreadUtil;
import org.rrlib.finroc_core_utils.log.LogDomain;
import org.rrlib.finroc_core_utils.log.LogLevel;

/**
 * @author max
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
@Ptr
@CppPrepend( { "rrlib::logging::LogDomainSharedPointer gcClassInitDomainDummy = GarbageCollector::_V_logDomain(); // make sure log domain exists" })
public class GarbageCollector extends LoopThread {

    /** Current tasks of Garbage Collector */
    private final ConcurrentQueue<DeferredDeleteTask> tasks = new ConcurrentQueue<DeferredDeleteTask>();

    /** Singleton instance - non-null while thread is running */
    private volatile static @Ptr GarbageCollector instance; /*= new GarbageCollector();*/

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
    @InCpp("_RRLIB_LOG_CREATE_NAMED_DOMAIN(logDomain, \"garbage_collector\");")
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
            logDomain.log(LogLevel.LL_DEBUG_WARNING, getLogDescription(), e);
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
        /*Cpp
        if (Thread::stoppingThreads()) {
            _FINROC_LOG_STREAM(rrlib::logging::eLL_WARNING, logDomain, "starting gc in this phase is not allowed");
            return;
        }
         */

        if (started == NO) {
            @SharedPtr GarbageCollector tmp = ThreadUtil.getThreadSharedPtr(new GarbageCollector());
            tmp.start();
            instance = tmp;
            started = YES;
        } else {
            //Cpp _FINROC_LOG_STREAM(rrlib::logging::eLL_WARNING, logDomain, "Cannot start gc thread twice. This attempt is possibly dangerous, since this method is not thread-safe");
        }
    }

    /**
     * @param t Thread
     * @return Is this the garbage collector?
     */
    public static boolean isGC(@SharedPtr Thread t) {
        return t != null && (t instanceof GarbageCollector);
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
     * (since this can be called by real-time threads, it must not block! - expect for program shutdown)
     *
     * @param elementToDelete Pointer to object that will be deleted
     */
    @JavaOnly
    public static void deleteDeferred(@CppType("SafeDestructible") @Ptr Object elementToDelete) {
        deleteDeferredImpl(elementToDelete);
    }

    /*Cpp
    // Safer than calling toString() directly - for use in log output
    static util::String getObjectString(util::Object* element) {
        String s("");
        finroc::util::Object* obj = dynamic_cast<finroc::util::Object*>(element);
        if (obj != NULL) {
          s = obj->toString();
        }
        return s;
    }
     */

    /**
     * Delete this object deferred (implementation)
     * (since this can be called by real-time threads, it must not block! - expect for program shutdown)
     *
     * @param elementToDelete Pointer to object that will be deleted
     */
    @InCppFile
    private static void deleteDeferredImpl(@CppType("SafeDestructible") @Ptr Object elementToDelete) {

        //Cpp _FINROC_LOG_STREAM(rrlib::logging::eLL_DEBUG_VERBOSE_1, logDomain, "Delete requested for: ", elementToDelete /*, " ", s.toCString()*/);
        @Ptr GarbageCollector gc = instance;
        if (gc == null) {
            assert(started == NO || ThreadUtil.getCurrentThreadId() == deleterThreadId);
            // safe to delete object now
            //Cpp elementToDelete->customDelete(true);
            return;
        }

        DeferredDeleteTask t = new DeferredDeleteTask(elementToDelete, Time.getCoarse() + SAFE_DELETE_INTERVAL);
        gc.tasks.enqueue(t);
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
            elementToDelete->customDelete(true);
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
