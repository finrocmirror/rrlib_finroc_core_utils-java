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

import org.rrlib.finroc_core_utils.jc.AtomicInt64;
import org.rrlib.finroc_core_utils.jc.annotation.InCpp;
import org.rrlib.finroc_core_utils.jc.annotation.Inline;
import org.rrlib.finroc_core_utils.jc.annotation.JavaOnly;
import org.rrlib.finroc_core_utils.jc.annotation.NoCpp;

/**
 * @author max
 *
 * Efficient, very simple lock variation.
 * Spin lock
 */
@Inline @NoCpp @JavaOnly
public class SpinLock {

    /** Stores uid of thread that currently has exclusive access - -1 means none */
    @InCpp("SpinMutex mutex;")
    private final AtomicInt64 mutex = new AtomicInt64(-1);

    /**
     * Wait until exclusive access is gained
     */
    @InCpp("SpinLock::_acquire(mutex);")
    public void lock() {
        while (!tryLock()) {}
    }

    /**
     * Try to gain exclusive access
     *
     * @return True: exclusive access gained - false: other thread currently has exclusive access
     */
    @InCpp("return SpinLock::_tryAcquire(mutex);")
    public boolean tryLock() {
        long id = ThreadUtil.getCurrentThreadId();
        return mutex.compareAndSet(-1, id);
    }

    /**
     * Release exclusive access
     */
    @InCpp("SpinLock::_release();")
    public void release() {
        assert(mutex.get() == ThreadUtil.getCurrentThreadId());
        mutex.set(-1);
    }

    @JavaOnly
    public boolean hasLock() {
        return mutex.get() == ThreadUtil.getCurrentThreadId();
    }
}