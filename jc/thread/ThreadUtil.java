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
package org.rrlib.finroc_core_utils.jc.thread;

import org.rrlib.finroc_core_utils.jc.annotation.InCpp;
import org.rrlib.finroc_core_utils.jc.annotation.Managed;
import org.rrlib.finroc_core_utils.jc.annotation.NoCpp;
import org.rrlib.finroc_core_utils.jc.annotation.PassByValue;
import org.rrlib.finroc_core_utils.jc.annotation.Prefix;
import org.rrlib.finroc_core_utils.jc.annotation.Ptr;
import org.rrlib.finroc_core_utils.jc.annotation.Ref;
import org.rrlib.finroc_core_utils.jc.annotation.SharedPtr;

/**
 * @author max
 *
 * Diverse static utility functions regarding threads.
 *
 * These utility functions are mainly helpful to handle
 * threads in C++ (also).
 */
@NoCpp @Prefix("s")
public class ThreadUtil {

    /**
     * Get Current thread id
     * (In C++ much faster than using Thread.currentThread.getId();
     *
     * @return Current thread id
     */
    @InCpp("return Thread::currentThreadRaw()->getId();")
    public static long getCurrentThreadId() {
        return Thread.currentThread().getId();
    }

    /**
     * Setup thread so that it will automatically delete itself
     * when it is finished and there are no further references
     * to it
     *
     * @param t Thread to set up
     * @return Relevant Further Thread reference
     */
    @InCpp( {"t.setAutoDelete();", "return t.getSharedPtr();"})
    public static @SharedPtr Thread setAutoDelete(@Ref Thread t) {
        return t;
    }

    /**
     * Get shared pointer to thread (the same that thread uses internally)
     * (only relevant for C++)
     * (Thread object will be deallocated when both thread has ended and any
     *  further references from this shared pointer are reset)
     *
     * @param t Thread to get pointer from
     * @return Shared pointer to thread
     */
    @InCpp( {"t->setAutoDelete(); // we want AutoDelete semantics now",
             "return std::static_pointer_cast<T>(t->getSharedPtr());"
            })
    public static @SharedPtr <T extends Thread> T getThreadSharedPtr(@Managed @Ptr T t) {
        return t;
    }

    /**
     * Makes a thread a real-time thread.
     * This currently only works in C++ with a real-time kernel.
     * (To do this in RT-Java we need a major restructuring in Thread class hierarchy:
     *  probably wrap either java.lang.Thread or javax.realtime.RealtimeThread)
     *
     * @param t Thread to make real-time
     */
    @InCpp("t->setRealtime();")
    public static void makeThreadRealtime(@PassByValue @SharedPtr Thread t) {
        t.setPriority(Thread.MAX_PRIORITY);
    }
}