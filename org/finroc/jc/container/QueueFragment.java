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

import org.finroc.jc.annotation.InCpp;
import org.finroc.jc.annotation.PassByValue;
import org.finroc.jc.annotation.Ptr;
import org.finroc.jc.annotation.RawTypeArgs;
import org.finroc.jc.log.LogDefinitions;
import org.finroc.jc.log.LogUser;
import org.finroc.log.LogDomain;
import org.finroc.log.LogLevel;

/**
 * @author max
 *
 * Queue fragment.
 *
 * Used to dequeue all elements of bounded WonderQueue at once
 */
@PassByValue @RawTypeArgs
public class QueueFragment<T, C extends BoundedQElementContainer> extends LogUser {

    //Cpp template <typename X, typename Y>
    //Cpp friend class WonderQueueBounded;

    /** Next element in fragment */
    protected C next = null;

    /** Last element in fragment - null if no more objects are left - will not be recycled, since it's still in queue */
    protected C last = null;

    /** Object of last element */
    @Ptr protected T lastObject = null;

    /** "Previous" entry of last element */
    protected C lastPrev = null;

    /** Skip first/next element */
    public boolean skipFirst = false;

    /** Log domain for this class */
    @InCpp("_RRLIB_LOG_CREATE_NAMED_DOMAIN(logDomain, \"queue_impl\");")
    private static final LogDomain logDomain = LogDefinitions.finrocUtil.getSubDomain("queue_impl");

    /**
     * Dequeue one element
     *
     * @return Next element in QueueFragment - null when there's none
     */
    @SuppressWarnings("unchecked")
    public @Ptr T dequeue() {
        @Ptr C n2 = next;
        if (last == null) {
            return null;
        } else if (n2 == last) {
            last = null;
            return skipFirst ? null : lastObject;
        } else {
            assert(n2 != null);
            @Ptr BoundedQElementContainer nextX = n2.next2.get();
            if (nextX.isDummy()) {
                // rare preemption case: find next element from the back

                if (n2 == lastPrev) {
                    next = last;
                } else {
                    @Ptr BoundedQElementContainer current = lastPrev;
                    while (current.prev != n2) {
                        current = current.prev;
                    }
                    //System.out.println("Rare preemption case: " + n2.toString() + " " + current.toString());
                    log(LogLevel.LL_DEBUG_VERBOSE_1, logDomain, "Rare preemption case: " + n2.toString() + " " + current.toString());
                    next = (C)current;
                }
                //assert(n2.count == current.count - 1);
                //n2 = current;
            } else {
                next = (C)nextX;
            }
        }
        @Ptr T result = (T)n2.element;

        n2.recycle(false);
        if (result == null || skipFirst) {
            skipFirst = false;
            result = (T)dequeue();
        }
        return result;
    }

    /**
     * @param recycleContents Recycle contents?
     */
    @SuppressWarnings("unchecked")
    public void clear(boolean recycleContents) {

        if (last == next) {
            if (!skipFirst && lastObject != null) {
                last.recycleContent(lastObject);  // last can be null and this (pseudo-static) method still works
            }
            return;
        }

        if (lastObject != null) {
            last.recycleContent(lastObject);  // last can be null and this (pseudo-static) method still works
        }

        // from back to start - so we won't have any issues with any "next"s that are set too late
        if (lastPrev != null) {
            C ec = lastPrev;
            while (true) {
                C prev = (C)ec.prev;
                ec.recycle(recycleContents && (!(skipFirst && ec == next)));
                if (ec == next) {
                    next = null;
                    last = null;
                    return;
                }
                ec = prev;
            }
        }
    }

}
