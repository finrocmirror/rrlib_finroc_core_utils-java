//
// You received this file as part of RRLib
// Robotics Research Library
//
// Copyright (C) Finroc GbR (finroc.org)
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//
//----------------------------------------------------------------------
package org.rrlib.finroc_core_utils.jc.container;

import org.rrlib.finroc_core_utils.jc.ArrayWrapper;
import org.rrlib.finroc_core_utils.jc.AtomicPtr;
import org.rrlib.finroc_core_utils.jc.thread.SpinLock;

/**
 * @author Max Reichardt
 *
 * Implementation of class below
 */
class RawWonderQueue extends Queueable {

    /** Spin lock for concurrent access */
    private final SpinLock lock = new SpinLock();

    /**
     * Pointer to last element in queue - never null
     * This variable us used for "communication" among writers
     */
    private final AtomicPtr<Queueable> last = new AtomicPtr<Queueable>(this);

    /**
     * Temporary last object (for dequeueing) - this way all elements can be dequeued.
     * points to this, if no element is available that has not been read already.
     */
    private Queueable readLast = this;

    /**
     * Next element after readLast
     *
     * This variable is used for "communication" between writer and reader.
     */
    private final AtomicPtr<Queueable> nextFirst = new AtomicPtr<Queueable>(null);

    public RawWonderQueue() {
        next = null;
    }

    /**
     * Add element to the end of the queue.
     * (Is thread safe and non-blocking/non-waiting)
     *
     * @param pd Element to enqueue
     */
    public void enqueueRaw(Queueable pd) {

        // swap last pointer
        Queueable prev = last.getAndSet(pd);

        if (prev == this) {
            assert(nextFirst.get() == null);
            nextFirst.set(pd);
        } else {
            // set "next" of previous element
            prev.next = pd;
        }
    }

    /**
     * Remove first element from queue and return it.
     * (May only be called by a single reader thread concurrently.)
     *
     * @return Element that was dequeued - null if no element is available
     */
    public Queueable dequeueRaw() {
        Queueable next = this.next;
        if (next == null) { // does readLast need updating?
            next = nextFirst.getAndSet(null);
            if (next == null) {
                return null; // queue empty
            }
            readLast = last.getAndSet(this);
        }

        if (next == readLast) {
            this.next = null;
            return next;
        }
        Queueable nextnext = next.next;
        if (nextnext == null) { // can occur with delayed/preempted enqueue operations (next is set later and is not volatile)
            return null; // queue is not empty, but elements are not fully available yet
        }
        this.next = nextnext;
        next.next = null;
        return next;
    }

    /**
     * Remove first element from queue and return it.
     * (May be called by multiple threads concurrently. Uses locks => not non-blocking)
     *
     * @return Element that was dequeued - null if no element is available
     */
    public Queueable concurrentDequeueRaw() {
        lock.lock();
        Queueable result = dequeueRaw();
        lock.release();
        return result;
    }

    /**
     * Dequeue multiple elements at once
     *
     * @param buffer Buffer to write result to
     * @param maxElements Maximum number of elements to dequeue
     * @return Actual number of elements dequeued (can be less if queue has lass elements)
     */
    @SuppressWarnings( { "rawtypes", "unchecked" })
    public int dequeueRaw(ArrayWrapper buffer, int maxElements) {
        int pos = 0;

        // first run: dequeue remaining elements in current chunk
        // second run: dequeue remaining elements in updated chunk
        for (int i = 0;; i++) {
            while (next != null && pos < maxElements) {
                if (next == readLast) {
                    next = null;
                }
                Queueable nextnext = next.next;
                if (nextnext == null) { // can occur with delayed/preempted enqueue operations (next is set later and is not volatile)
                    return pos; // queue is not empty, but elements are not fully available yet
                }
                next = nextnext;
                next.next = null;
                buffer.set(pos, next);
                pos++;
            }

            if (i == 1 || pos == maxElements) { // terminate after second run
                return pos;
            }

            // fetch updated chunk
            next = nextFirst.getAndSet(null);
            if (next == null) {
                return pos; // queue empty
            }
            readLast = last.getAndSet(this);
        }
    }
}

/**
 * @author Max Reichardt
 *
 * This is a special concurrent non-blocking FIFO linked queue.
 * It should be real-time-capable, since it does not need to allocate memory.
 *
 * It allows concurrent enqueueing operations and there is no size limit.
 * Unlike RawWonderQueueFastCR, only a single thread may read from this queue.
 *
 * In this variant, all elements can be dequeued.
 * Internally, it does this by dequeueing all elements internally and then returning
 * them one by one.
 */
public class WonderQueue<T extends Queueable> extends RawWonderQueue {

    /**
     * Add element to the end of the queue.
     * Is thread safe and non-blocking/non-waiting
     *
     * @param pd Element to enqueue
     */
    public void enqueue(T pd) {
        enqueueRaw(pd);
    }

    /**
     * Remove first element from queue and return it.
     * May only be called by a single reader thread concurrently.
     *
     * @return Element that was dequeued - null if no elements available
     */
    @SuppressWarnings("unchecked")
    public T dequeue() {
        return (T)dequeueRaw();
    }

    /**
     * Dequeue multiple elements at once
     *
     * @param buffer Buffer to write result to
     * @param maxElements Maximum number of elements to dequeue
     * @return Actual number of elements dequeued (can be less if queue has lass elements)
     */
    public int dequeue(ArrayWrapper<T> buffer, int maxElements) {
        return dequeueRaw(buffer, maxElements);
    }

    /**
     * Remove first element from queue and return it.
     * (May be called by multiple threads concurrently. Uses locks => not non-blocking)
     *
     * @return Element that was dequeued - null if no element is available
     */
    @SuppressWarnings("unchecked")
    public T concurrentDequeue() {
        return (T)concurrentDequeueRaw();
    }

    /**
     * (meant for use in destructors)
     * Delete all elements that are enqueued in this list
     */
    public void deleteEnqueued() {
        while (true) {
            T r = dequeue();
            if (r == null) {
                break;
            }
            r.delete();
        }
    }
}
