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
package org.finroc.jc.container;

import org.finroc.jc.AtomicInt;
import org.finroc.jc.HasDestructor;
import org.finroc.jc.Time;
import org.finroc.jc.annotation.CppInclude;
import org.finroc.jc.annotation.CppType;
import org.finroc.jc.annotation.ForwardDecl;
import org.finroc.jc.annotation.InCpp;
import org.finroc.jc.annotation.InCppFile;
import org.finroc.jc.annotation.JavaOnly;
import org.finroc.jc.annotation.PassByValue;
import org.finroc.jc.annotation.Ptr;
import org.finroc.jc.annotation.SharedPtr;
import org.finroc.jc.annotation.SizeT;

/**
 * @author max
 *
 * Counts allocations of various kinds of object/containers.
 *
 * Furthermore, global register for all reusable objects that should be assigned an application-unique 32bit Integer.
 * Allows associating the unique integer with an object - which in turn allows storing a stamped pointer in a long variable.
 */
@ForwardDecl( {AbstractReusable.class})
@CppInclude("AbstractReusable.h")
@SharedPtr
public class AllocationRegister implements HasDestructor {

    /** Number of reusables objects allocated */
    private AtomicInt reusables = new AtomicInt();

    /** Time when Runtime was created */
    @SuppressWarnings("unused")
    private final long startTime = Time.getPrecise();

    /** Singleton instance */
    @JavaOnly
    private static AllocationRegister instance;

    /**
     * Raw pointer to singleton instance - used in static unregisterReusable -
     * is safe, because it is still accessible after static deallocation of this class
     * and it can be ensured that instance is set to null after
     * every reusable has unregistered
     */
    @Ptr private static AllocationRegister rawInstance;

    /** List of tracked reusables */
    @SuppressWarnings("unused")
    private final SimpleListWithMutex<AbstractReusable> trackedReusables = new SimpleListWithMutex<AbstractReusable>(0x7FFFFFFF - 5);

    /** Initial size of indexed-Reusables register */
    private static final int INITIAL_REUSABLES_INDEX_SIZE = 128000;

    /** ArrayList that contains all reusables that should be indexed - index in list is "official" index */
    @PassByValue
    private SimpleListWithMutex<AbstractReusable> indexedReusables = new SimpleListWithMutex<AbstractReusable>(INITIAL_REUSABLES_INDEX_SIZE, 0x7FFFFFFF - 5);

    /** Queue with free(d) slots in indexedReusables, TODO: needn't be concurrent */
    @PassByValue
    @CppType("ConcurrentQueue<int>")
    private ConcurrentQueue<Integer> freeSlotQueue = new ConcurrentQueue<Integer>();


    public static @SharedPtr AllocationRegister getInstance() {
        //Cpp static ::std::tr1::shared_ptr<AllocationRegister> instance(new AllocationRegister());
        //Cpp rawInstance = instance._get();

        //JavaOnlyBlock
        synchronized (AllocationRegister.class) {
            if (instance == null && (!shuttingDown())) {
                instance = new AllocationRegister();
                rawInstance = instance;
            }
        }

        return instance;
    }

    /**
     * @return Is application shutting down?
     */
    @InCpp("return Thread::stoppingThreads();")
    private static boolean shuttingDown() {
        return false;
    }

    /**
     * Basic registration of reusable (for statistics mainly)
     *
     * @param r Reusable
     */
    public void registerReusable(AbstractReusable r) {
        int num = reusables.incrementAndGet();
        interpretNum(num);
    }

    /**
     * Track usage of reusable
     * (only actually done in debug mode - since significantly more memory consumption and CPU load)
     *
     * @param r Reusable
     */
    public void trackReusable(AbstractReusable r) {
        /*Cpp
        #ifndef NDEBUG
        Lock lock1(trackedReusables);
        trackedReusables.add(r);
        #endif
        */
    }

    /**
     * Acquire index for reusable object
     *
     * @param reusable Reusable object
     * @return Index/Handle in register
     */
    int indexReusable(AbstractReusable reusable) {
        synchronized (indexedReusables) {
            if (indexedReusables.size() == 0) {
                indexedReusables.add(null); // first element should be null element
            }
            @InCpp("int free = freeSlotQueue.dequeue();")
            Integer free = freeSlotQueue.dequeue();
            if (freeSlotQueue.dequeueSuccessful(free)) {
                indexedReusables.set(free, reusable);
                return free;
            } else {
                indexedReusables.add(reusable);
                return indexedReusables.size() - 1;
            }
        }
    }

    /**
     * Get Reusable object from register by index
     *
     * @param index Index of Reusable
     * @return Reusable
     */
    public static AbstractReusable getByIndex(int index) {
        assert(rawInstance != null);
        return rawInstance.indexedReusables.get(index);
    }

    /**
     * Unregister reusable object
     * (clears entries in all registers)
     *
     * @param r Reusable object
     */
    public static void unregisterReusable(AbstractReusable r) {
        assert(rawInstance != null);

        if (r.getRegisterIndex() >= 0) {
            @SizeT int idx = r.getRegisterIndex();
            synchronized (rawInstance.indexedReusables) {
                rawInstance.indexedReusables.set(idx, null);
                rawInstance.freeSlotQueue.enqueue(idx);
            }
        }

        //Cpp #ifndef NDEBUG
        rawInstance.reusables.decrementAndGet();
        /*Cpp
        Lock lock1(rawInstance->trackedReusables);
        rawInstance->trackedReusables.removeElem(r);
        #endif
        */
    }

    /**
     * Interpret number of allocated objects
     *
     * @param num Number of allocated objects
     */
    private void interpretNum(int num) {
        if ((num % 100) == 0) {
            System.out.println("Allocated " + reusables.get() + " Reusables");
        }
    }

    @InCppFile
    public void delete() {
        rawInstance = null;
        /*Cpp
        #ifndef NDEBUG
        printf("\nLeaked Reusable Object Report:\n");
        if (trackedReusables.size() == 0) {
            printf("  No reusables leaked. Valgrind should appreciate this.\n");
            return;
        }

        for (size_t i = 0; i < trackedReusables.size(); i++) {
            AbstractReusable* ar = trackedReusables.get(i);
            printf("%s %p \n", typeid(*ar).name(), ar);
            ar->printTrace(startTime);
        }
        #endif
        */
    }
}
