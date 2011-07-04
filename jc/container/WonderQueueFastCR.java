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
package org.rrlib.finroc_core_utils.jc.container;

import org.rrlib.finroc_core_utils.jc.annotation.Inline;
import org.rrlib.finroc_core_utils.jc.annotation.NoCpp;
import org.rrlib.finroc_core_utils.jc.annotation.NonVirtual;
import org.rrlib.finroc_core_utils.jc.annotation.Ptr;
import org.rrlib.finroc_core_utils.jc.annotation.RawTypeArgs;

/**
 * @author max
 *
 * (CR = "Concurrent Read")
 *
 * This is a special concurrent non-blocking FIFO linked queue.
 * It should be real-time-capable, since it does not need to allocate memory.
 *
 * It allows concurrent enqueueing operations and there is no size limit.
 * It's currently the only WonderQueue variant, that also allows non-blocking concurrent reads.
 *
 * This queue is faster than RawWonderQueue, but slower that RawWonderQueueFast.
 * Drawback: the queue always contains at least one element...
 * (for efficiency reasons; problem
 * is that 'last' will point to reused element if last element is dequeued; maybe
 * there's a better way (?) )
 */
@Inline @NoCpp @RawTypeArgs
public class WonderQueueFastCR<T extends Queueable> extends RawWonderQueueFast {

    public WonderQueueFastCR() {
        super(true);
    }

    /**
     * Add element to the end of the queue.
     * Is thread safe and non-blocking/non-waiting
     *
     * @param pd Element to enqueue
     */
    @NonVirtual @Inline public void enqueue(@Ptr T pd) {
        enqueueRaw(pd);
    }

    /**
     * Remove first element from queue and return it.
     * May only be called by a single reader thread concurrently.
     *
     * @return Element that was dequeued - null if no elements available
     */
    @SuppressWarnings("unchecked")
    @NonVirtual @Inline public @Ptr T dequeue() {
        return (T)concurrentDequeueRaw();
    }

    /**
     * (meant for use in destructors)
     * Delete all elements that are enqueued in this list
     */
    public void deleteEnqueued() {
        while (true) {
            @Ptr T r = dequeue();
            if (r == null) {
                break;
            }

            //JavaOnlyBlock
            r.delete();

            //Cpp r->customDelete(false);
        }

        // delete last element
        @Ptr Queueable last = this.nextCR.get();
        if (last != Queueable.terminator && last != this) {

            //JavaOnlyBlock
            last.delete();

            //Cpp last->customDelete(false);
        }
    }
}
