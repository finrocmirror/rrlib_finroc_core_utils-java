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

import org.finroc.jc.AutoDeleter;
import org.finroc.jc.annotation.CppInclude;
import org.finroc.jc.annotation.CppPrepend;
import org.finroc.jc.annotation.ForwardDecl;
import org.finroc.jc.annotation.Friend;
import org.finroc.jc.annotation.InCpp;
import org.finroc.jc.annotation.InCppFile;
import org.finroc.jc.annotation.Ptr;
import org.finroc.jc.annotation.SizeT;

/**
 * @author max
 *
 * Java-Only class that is a global register for all reusable objects that should be registered.
 *
 * Allows associating a unique integer with an object - which in turn allows storing a stamped pointer in a long variable.
 */
@Friend(AbstractReusable.class) @ForwardDecl(AbstractReusable.class)
@CppInclude("AbstractReusable.h")
@CppPrepend("ConcurrentQueue<int>* ReusablesRegister::freeSlotQueue;")
public class ReusablesRegister {

    /** Initial register size */
    private static final int INITIAL_SIZE = 128000;

    /** ArrayList that contains entries */
    @Ptr
    public static SimpleList<AbstractReusable> backend = null;// = new SimpleList<AbstractReusable>(INITIAL_SIZE);

    /** Queue with free(d) slots in backend */
    @Ptr
    @InCpp("static ConcurrentQueue<int>* freeSlotQueue;")
    public static ConcurrentQueue<Integer> freeSlotQueue = null; // = new ConcurrentQueue<Integer>();

    /**
     * Register reusable object
     * (called when such an object is created)
     * (synchronized, because array may grow)
     *
     * @param reusable Reusable object
     * @return Index/Handle in register
     */
    protected static synchronized int register(AbstractReusable reusable) {
        if (backend == null) {
            backend = AutoDeleter.addStatic(new SimpleList<AbstractReusable>(INITIAL_SIZE));

            // JavaOnlyBlock
            freeSlotQueue = AutoDeleter.addStatic(new ConcurrentQueue<Integer>());

            //Cpp freeSlotQueue = AutoDeleter::addStatic(new ConcurrentQueue<int>());
        }

        if (backend.size() == 0) {
            backend.add(null); // first element should be null element
        }
        @InCpp("int free = freeSlotQueue->dequeue();")
        Integer free = freeSlotQueue.dequeue();
        if (freeSlotQueue.dequeueSuccessful(free)) {
            backend.set(free, reusable);
            return free;
        } else {
            backend.add(reusable);
            return backend.size() - 1;
        }
    }

    /**
     * Unregister reusable object
     * (called when such an object is deleted)
     *
     * @param reusable Reusable object
     */
    @InCppFile
    protected static synchronized void unregister(AbstractReusable reusable) {
        @SizeT int idx = reusable.getRegisterIndex();
        backend.set(idx, null);
        freeSlotQueue.enqueue(idx);
    }

    /**
     * Get Reusable object from register
     *
     * @param index Index of object
     * @return Object
     */
    public static AbstractReusable get(int index) {
        assert(backend != null);
        return backend.get(index);
    }


}
