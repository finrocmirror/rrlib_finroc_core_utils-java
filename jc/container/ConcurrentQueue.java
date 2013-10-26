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

import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * @author Max Reichardt
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
public class ConcurrentQueue<T> {

    /** Wrapped Queue (in C++ there's no peek - so no peek...) */
    private final ConcurrentLinkedQueue<T> backend;

    public ConcurrentQueue() {
        backend = new ConcurrentLinkedQueue<T>();
    }

    /**
     * @param element Element to enqueue
     */
    public void enqueue(T element) {
        backend.add(element);
    }

    /**
     * Removes first element from queue
     * (This should be used when values are stored by value - relevant for C++)
     *
     * @return Removed element
     */
    public T dequeue() {
        return backend.poll();
    }

    /**
     * Did last dequeue operation return something valid ? (or was queue empty?)
     *
     * @param t Dequeued element
     * @return Answer
     */
    public boolean dequeueSuccessful(T t) {
        return t != null;
    }

    /**
     * Removes first element from queue
     * (This should/can be used when pointers are used - relevant for C++)
     *
     * @return Removed element
     */
    public T dequeuePtr() {
        return backend.poll();
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
    public boolean isEmpty() {
        return backend.isEmpty();
    }
}
