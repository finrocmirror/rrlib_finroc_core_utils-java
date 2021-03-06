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
package org.rrlib.finroc_core_utils.jc.container;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.rrlib.finroc_core_utils.jc.Time;
import org.rrlib.logging.Log;
import org.rrlib.logging.LogDomainRegistry;
import org.rrlib.logging.LogLevel;

/**
 * @author Max Reichardt
 *
 * Counts allocations of various kinds of object/containers.
 *
 * Furthermore, global register for all reusable objects that should be assigned an application-unique 32bit Integer.
 * Allows associating the unique integer with an object - which in turn allows storing a stamped pointer in a long variable.
 */
public class AllocationRegister {

    /** Number of reusables objects allocated */
    private AtomicInteger reusables = new AtomicInteger();

    /** Time when Runtime was created */
    @SuppressWarnings("unused")
    private final long startTime = Time.getPrecise();

    /** Singleton instance */
    private static AllocationRegister instance;

    /**
     * Raw pointer to singleton instance - used in static unregisterReusable -
     * is safe, because it is still accessible after static deallocation of this class
     * and it can be ensured that instance is set to null after
     * every reusable has unregistered
     */
    private static AllocationRegister rawInstance;

    /** List of tracked reusables */
    @SuppressWarnings("unused")
    private final ArrayList<AbstractReusable> trackedReusables = new ArrayList<AbstractReusable>();

    /** Initial size of indexed-Reusables register */
    private static final int INITIAL_REUSABLES_INDEX_SIZE = 128000;

    /** ArrayList that contains all reusables that should be indexed - index in list is "official" index */
    private ArrayList<AbstractReusable> indexedReusables = new ArrayList<AbstractReusable>(INITIAL_REUSABLES_INDEX_SIZE);

    /** Queue with free(d) slots in indexedReusables, TODO: needn't be concurrent */
    private ArrayDeque<Integer> freeSlotQueue = new ArrayDeque<Integer>();

    /** "Lock" on log domain registry - to prevent domains from being deallocated before AllocationRegister */
    @SuppressWarnings("unused")
    private LogDomainRegistry logRoot = LogDomainRegistry.getInstance();

    public AllocationRegister() {
    }

    public static AllocationRegister getInstance() {
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

            boolean empty = freeSlotQueue.isEmpty();
            if (!empty) {
                Integer free = freeSlotQueue.pop();
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
            int idx = r.getRegisterIndex();
            synchronized (rawInstance.indexedReusables) {
                rawInstance.indexedReusables.set(idx, null);
                rawInstance.freeSlotQueue.push(idx);
            }
        }

        rawInstance.reusables.decrementAndGet();
    }

    /**
     * Interpret number of allocated objects
     *
     * @param num Number of allocated objects
     */
    private void interpretNum(int num) {
        if ((num % 100) == 0) {
            Log.log(LogLevel.DEBUG_VERBOSE_1, this, "Allocated " + reusables.get() + " Reusables");
        }
    }
}
