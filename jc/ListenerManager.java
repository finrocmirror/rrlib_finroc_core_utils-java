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
package org.rrlib.finroc_core_utils.jc;

import java.util.ArrayList;

import org.rrlib.finroc_core_utils.jc.container.SafeConcurrentlyIterableList;

/**
 * @author Max Reichardt
 *
 * Generic manager for listeners (observer pattern)
 *
 * Allows notifications concurrently to add/remove-operations
 */
public abstract class ListenerManager < ORIGIN, PARAMETER, LISTENERTYPE, T extends ListenerManager < ORIGIN, PARAMETER, LISTENERTYPE, ? >> {

    /** Single listener - unused, but != NULL when there are more than one listeners */
    private LISTENERTYPE listener;

    /** If we have more than a single listener - this list will be created and contains ALL listeners */
    private SafeConcurrentlyIterableList<LISTENERTYPE> listenerList = null;

    /** Mutex for list - Since we call garbage collector lock for list needs to be before in order */
    public final MutexLockOrder objMutex = new MutexLockOrder(Integer.MAX_VALUE - 40);

    public ListenerManager() {
        //this(false);
    }

    /**
     * @param listener Listener to add
     */
    public synchronized void add(LISTENERTYPE listener) {
        if (this.listener == null) {
            this.listener = listener;
        } else {

            // is listener already in list? ... then return and do nothing
            if (listenerList != null) {
                ArrayWrapper<LISTENERTYPE> it = listenerList.getIterable();
                for (int i = 0, n = it.size(); i < n; i++) {
                    LISTENERTYPE lt = it.get(i);
                    if (lt == listener) {
                        return;
                    }
                }
            } else if (this.listener == listener) {
                return;
            }

            if (listenerList == null) {
                listenerList = new SafeConcurrentlyIterableList<LISTENERTYPE>(2, 4);
                listenerList.add(this.listener, false);
            }
            listenerList.add(listener, false);
        }
    }

    /**
     * @param listener Listener to remove
     */
    public synchronized void remove(LISTENERTYPE listener2) {
        if (listenerList != null) {
            listenerList.remove(listener2);
        } else if (listener == listener2) {
            listener = null;
        }
    }

    /**
     * Notify listeners
     *
     * @param origin Source of event
     * @param parameter Parameter of event
     */
    public void notify(ORIGIN origin, PARAMETER parameter) {
        if (listener != null) {
            notify2(origin, parameter, 0);
        }
    }

    /**
     * Notify listeners
     *
     * @param origin Source of event
     * @param parameter Parameter of event
     * @param callId ID of method to call
     */
    public void notify(ORIGIN origin, PARAMETER parameter, int callId) {
        if (listener != null) {
            notify2(origin, parameter, callId);
        }
    }

    /**
     * Second step - split up in order to inline/accelerate things,
     * when there are no listeners
     *
     * @param origin Source of event
     * @param parameter Parameter of event
     * @param callId ID of method to call
     */
    @SuppressWarnings("unchecked")
    private void notify2(ORIGIN origin, PARAMETER parameter, int callId) {
        if (listener != null) {
            if (listenerList != null) {
                ArrayWrapper<LISTENERTYPE> it = listenerList.getIterable();
                for (int i = 0, n = it.size(); i < n; i++) {
                    LISTENERTYPE lt = it.get(i);
                    if (lt != null) {
                        ((T)this).singleNotify(lt, origin, parameter, callId);
                    }
                }
            } else {
                ((T)this).singleNotify(listener, origin, parameter, callId);
            }
        }
    }

    /**
     * @param result List to write result to: Contains all current listeners after call
     */
    public void getListenersCopy(ArrayList<LISTENERTYPE> result) {
        result.clear();
        if (listenerList != null) {
            ArrayWrapper<LISTENERTYPE> it = listenerList.getIterable();
            for (int i = 0, n = it.size(); i < n; i++) {
                LISTENERTYPE lt = it.get(i);
                if (lt != null) {
                    result.add(lt);
                }
            }
        } else if (listener != null) {
            result.add(listener);
        }
    }

    /**
     * Notify single listener
     * (needs to be overridden by subclass)
     * (public, because protected doesn't work in C++ with casts that optimize virtual method calls away)
     *
     * @param origin Source of event
     * @param parameter Parameter of event
     * @param listener Listener to notify
     */
    public void singleNotify(LISTENERTYPE listener, ORIGIN origin, PARAMETER parameter, int callId) {}
}
