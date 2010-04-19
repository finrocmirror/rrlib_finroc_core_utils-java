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
package org.finroc.jc;

import org.finroc.jc.annotation.Const;
import org.finroc.jc.annotation.InCpp;
import org.finroc.jc.annotation.Include;
import org.finroc.jc.annotation.Init;
import org.finroc.jc.annotation.Inline;
import org.finroc.jc.annotation.NonVirtual;
import org.finroc.jc.annotation.Ptr;
import org.finroc.jc.annotation.SizeT;
import org.finroc.jc.container.SafeConcurrentlyIterableList;

/**
 * @author max
 *
 * Generic manager for listeners (observer pattern)
 *
 * Allows notifications concurrently to add/remove-operations
 */
@Include("container/SafeConcurrentlyIterableList.h")
public abstract class ListenerManager<ORIGIN, PARAMETER, LISTENERTYPE, T extends ListenerManager<ORIGIN, PARAMETER, LISTENERTYPE, ?>> {

    /** Single listener - unused, but != NULL when there are more than one listeners */
    private @Ptr LISTENERTYPE listener;

    /** If we have more than a single listener - this list will be created and contains ALL listeners */
    @InCpp("SafeConcurrentlyIterableList<_LISTENERTYPE*, 4>* listenerList;")
    private @Ptr SafeConcurrentlyIterableList<LISTENERTYPE> listenerList = null;

//  /** Only relevant for C++ - Delete all listeners when listener manager is deleted? */
//  private boolean deleteListenersOnDestruction;

    @Init("listenerList(NULL)")
    public ListenerManager() {
        //this(false);
    }

//  /**
//   * @param deleteListenersOnDestruction2 Only relevant for C++ - Delete all listeners when listener manager is deleted?
//   */
//  @Init("listenerList(NULL)")
//  public ListenerManager(boolean deleteListenersOnDestruction2) {
//      deleteListenersOnDestruction = deleteListenersOnDestruction2;
//  }

    /*Cpp
    virtual ~ListenerManager() {
        delete listenerList;
    }
     */

    /**
     * @param listener Listener to add
     */
    public synchronized void add(@Ptr LISTENERTYPE listener) {
        if (this.listener == null) {
            this.listener = listener;
        } else {

            // is listener already in list? ... then return and do nothing
            if (listenerList != null) {
                @InCpp("ArrayWrapper<_LISTENERTYPE*>* it = listenerList->getIterable();")
                @Ptr ArrayWrapper<LISTENERTYPE> it = listenerList.getIterable();
                for (@SizeT int i = 0, n = it.size(); i < n; i++) {
                    @InCpp("_LISTENERTYPE* lt = it->get(i);")
                    @Ptr LISTENERTYPE lt = it.get(i);
                    if (lt == listener) {
                        return;
                    }
                }
            } else if (this.listener == listener) {
                return;
            }

            //JavaOnlyBlock
            if (listenerList == null) {
                listenerList = new SafeConcurrentlyIterableList<LISTENERTYPE>(2, 4);
                listenerList.add(this.listener, false);
            }
            listenerList.add(listener, false);

            /*Cpp
            if (listenerList == NULL)
            {
              listenerList = new SafeConcurrentlyIterableList<_LISTENERTYPE*>(2, 4);
              listenerList->add(this->listener, false);
            }
            listenerList->add(listener_, false);
            */


        }
    }

    /**
     * @param listener Listener to remove
     */
    public synchronized void remove(@Ptr LISTENERTYPE listener2) {

        //JavaOnlyBlock
        if (listenerList != null) {
            listenerList.remove(listener2);
        } else if (listener == listener2) {
            listener = null;
        }

        /*Cpp
        if (listenerList != NULL) {
            listenerList->remove(listener2);
        } else if (listener == listener2) {
            listener = NULL;
        }
        */
    }

    /**
     * Notify listeners
     *
     * @param origin Source of event
     * @param parameter Parameter of event
     */
    @Inline public void notify(@Ptr ORIGIN origin, @Const @Ptr PARAMETER parameter) {
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
    @Inline public void notify(@Ptr ORIGIN origin, @Const @Ptr PARAMETER parameter, int callId) {
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
    private void notify2(@Ptr ORIGIN origin, @Const @Ptr PARAMETER parameter, int callId) {
        if (listener != null) {
            if (listenerList != null) {
                @InCpp("ArrayWrapper<_LISTENERTYPE*>* it = listenerList->getIterable();")
                @Ptr ArrayWrapper<LISTENERTYPE> it = listenerList.getIterable();
                for (@SizeT int i = 0, n = it.size(); i < n; i++) {
                    @InCpp("_LISTENERTYPE* lt = it->get(i);")
                    @Ptr LISTENERTYPE lt = it.get(i);
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
     * Notify single listener
     * (needs to be overridden by subclass)
     * (public, because protected doesn't work in C++ with casts that optimize virtual method calls away)
     *
     * @param origin Source of event
     * @param parameter Parameter of event
     * @param listener Listener to notify
     */
    @NonVirtual public void singleNotify(@Ptr LISTENERTYPE listener, @Ptr ORIGIN origin, @Const @Ptr PARAMETER parameter, int callId) {}
    // non-virtual, because we have cast to T in C++
}
