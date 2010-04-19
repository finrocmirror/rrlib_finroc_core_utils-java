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
import org.finroc.jc.annotation.ForwardDecl;
import org.finroc.jc.annotation.InCpp;
import org.finroc.jc.annotation.InCppFile;
import org.finroc.jc.annotation.Ptr;
import org.finroc.jc.annotation.SharedPtr;

/**
 * @author max
 *
 * Counts allocations of various kinds of object/containers.
 * Only used for debugging in order to find leaks
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
    private static AllocationRegister instance;

    /**
     * Raw pointer to singleton instance - used in static unregisterReusable -
     * is safe, because it can be ensured that instance is set to null after
     * every reusable has unregistered
     */
    @Ptr private static AllocationRegister rawInstance;

    /** List of tracked reusables */
    @SuppressWarnings("unused")
    private final SimpleList<AbstractReusable> trackedReusables = new SimpleList<AbstractReusable>();

    public static synchronized @SharedPtr AllocationRegister getInstance() {
        if (instance == null && (!shuttingDown())) {
            instance = new AllocationRegister();
            rawInstance = instance;
        }
        return instance;
    }

    @InCpp("return Thread::stoppingThreads();")
    private static boolean shuttingDown() {
        return false;
    }

    public void registerReusable(AbstractReusable r) {
        int num = reusables.incrementAndGet();
        interpretNum(num);
    }

    public void trackReusable(AbstractReusable r) {
        /*Cpp
        #ifndef NDEBUG
        trackedReusables.add(r);
        #endif
        */
    }

    public static void unregisterReusable(AbstractReusable r) {
        //Cpp #ifndef NDEBUG
        assert(rawInstance != null);
        rawInstance.reusables.decrementAndGet();
        /*Cpp
        rawInstance->trackedReusables.removeElem(r);
        #endif
        */
    }

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
            printf("  No reusables leaked. Valgrind loves you.\n");
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
