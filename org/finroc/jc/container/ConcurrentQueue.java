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

import java.util.concurrent.ConcurrentLinkedQueue;

import org.finroc.jc.annotation.Const;
import org.finroc.jc.annotation.ConstMethod;
import org.finroc.jc.annotation.CppPrepend;
import org.finroc.jc.annotation.InCpp;
import org.finroc.jc.annotation.Include;
import org.finroc.jc.annotation.Init;
import org.finroc.jc.annotation.Inline;
import org.finroc.jc.annotation.PassByValue;
import org.finroc.jc.annotation.Ref;

/**
 * @author max
 *
 * This is a wait-free, fully concurrent FIFO queue.
 *
 * Dequeueing is somewhat tricky - at least when enqueueing values and
 * trying to use the same syntax in C++ and Java (because a value cannot
 * be compared to NULL - in case the queue is empty)
 *
 * Easy case - when pointers are used in C++:
 *
 *  T t = queue.dequeuePtr();
 *  if (t != null) { ... }
 *
 * Tricky case - when values are used in C++ (also works with pointers):
 *
 *  T t = queue.dequeue();
 *  if (dequeueSuccessful(t)) { ... }
 *
 * DequeueSuccessful() should be called directly after dequeue() (and definitely
 * before any other access to a ConcurrentQueue, because the result is stored
 * in a ThreadLocal in C++ - this seemed the most elegant/universal and efficient
 * implementation that works with both Java and C++.
 * Feel free to come up with something better.
 */
@Inline
@Include("<tbb/concurrent_queue.h>")
@CppPrepend( {"template<typename T>",
              "__thread bool ConcurrentQueue<T>::success = false;"
             })
public class ConcurrentQueue<T> {

    /*Cpp
    // Did last dequeue operation return something valid ?
    static __thread bool success;
     */

    /** Wrapped Queue (in C++ there's no peek - so no peek...) */
    @InCpp("tbb::concurrent_queue<T> backend;")
    private final ConcurrentLinkedQueue<T> backend;

    @Init("backend()")
    public ConcurrentQueue() {
        backend = new ConcurrentLinkedQueue<T>();
    }

    /*Cpp
    // true, if there was an element to dequeue
    inline bool dequeue(T& t) {
    #if TBB_VERSION_MAJOR >= 2 && TBB_VERSION_MINOR >= 2
        return backend.try_pop(t);
    #else
        return backend.pop_if_present(t);
    #endif
    }
     */

    /**
     * @param element Element to enqueue
     */
    @InCpp("backend._push(element);")
    public void enqueue(@Const @Ref T element) {
        backend.add(element);
    }

    /**
     * Removes first element from queue
     * (This should be used when values are stored by value - relevant for C++)
     *
     * @return Removed element
     */
    public @PassByValue T dequeue() {

        // JavaOnlyBlock
        return backend.poll();

        /*Cpp
        T t;
        #if TBB_VERSION_MAJOR >= 2 && TBB_VERSION_MINOR >= 2
        success = backend.try_pop(t);
        #else
        success = backend.pop_if_present(t);
        #endif
        return t;
         */
    }

    /**
     * Did last dequeue operation return something valid ? (or was queue empty?)
     *
     * @param t Dequeued element
     * @return Answer
     */
    @ConstMethod @Inline @InCpp("return success;")
    public boolean dequeueSuccessful(@Const T t) {
        return t != null;
    }

    /**
     * Removes first element from queue
     * (This should/can be used when pointers are used - relevant for C++)
     *
     * @return Removed element
     */
    public T dequeuePtr() {

        // JavaOnlyBlock
        return backend.poll();

        /*Cpp
        T t = NULL;
        #if TBB_VERSION_MAJOR >= 2 && TBB_VERSION_MINOR >= 2
        backend.try_pop(t);
        #else
        backend.pop_if_present(t);
        #endif
        return t;
         */
    }

    /**
     * @return Returns first element of queue without deleting it
     */
    /*public T peek() {
        return backend.peek();
    }*/

    /**
     * @return is queue currently empty?
     */
    @ConstMethod @InCpp("return backend._empty();")
    public boolean isEmpty() {
        return backend.isEmpty();
    }
}
