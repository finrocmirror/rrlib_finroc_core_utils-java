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
package org.rrlib.finroc_core_utils.jc.thread;


/**
 * @author Max Reichardt
 *
 * Diverse static utility functions regarding threads.
 *
 * These utility functions are mainly helpful to handle
 * threads in C++ (also).
 */
public class ThreadUtil {

    /**
     * Get Current thread id
     * (In C++ much faster than using Thread.currentThread.getId();
     *
     * @return Current thread id
     */
    public static long getCurrentThreadId() {
        return Thread.currentThread().getId();
    }


    /**
     * Makes a thread a real-time thread.
     * This currently only works in C++ with a real-time kernel.
     * (To do this in RT-Java we need a major restructuring in Thread class hierarchy:
     *  probably wrap either java.lang.Thread or javax.realtime.RealtimeThread)
     *
     * @param t Thread to make real-time
     */
    public static void makeThreadRealtime(Thread t) {
        t.setPriority(Thread.MAX_PRIORITY);
    }
}
