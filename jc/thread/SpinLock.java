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

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Max Reichardt
 *
 * Efficient, very simple lock variation.
 * Spin lock
 */
public class SpinLock {

    /** Stores uid of thread that currently has exclusive access - -1 means none */
    private final AtomicLong mutex = new AtomicLong(-1);

    /**
     * Wait until exclusive access is gained
     */
    public void lock() {
        while (!tryLock()) {}
    }

    /**
     * Try to gain exclusive access
     *
     * @return True: exclusive access gained - false: other thread currently has exclusive access
     */
    public boolean tryLock() {
        long id = ThreadUtil.getCurrentThreadId();
        return mutex.compareAndSet(-1, id);
    }

    /**
     * Release exclusive access
     */
    public void release() {
        assert(mutex.get() == ThreadUtil.getCurrentThreadId());
        mutex.set(-1);
    }

    public boolean hasLock() {
        return mutex.get() == ThreadUtil.getCurrentThreadId();
    }
}
