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

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Max Reichardt
 *
 * Container for one element in bounded wonder qeueue.
 */
public abstract class BoundedQElementContainer extends Reusable {

    /** Element containers which have this as object are dummy containers */
    static final Object DUMMY_MARKER = new Object();

    /** Number of "dummy elements" - must be 2^x */
    private static final int DUMMY_ELEMENTS = 16;

    /** Mask for dummy element index */
    private static final int DUMMY_MASK = DUMMY_ELEMENTS - 1;

    /** Have dummies been initiliazed? */
    private static boolean initialized = false;

    /** Dummy elements - purpose: safe setting of next2 of Elements */
    private static BoundedQElementContainer[] dummies;

    /** Element in this container */
    protected volatile Object element;

    /**
     * @return Element in this container
     */
    public Object getElement() {
        return element;
    }

    /**
     * @param Element in this container to set
     */
    public void setElement(Object element) {
        this.element = element;
    }

    /** Previous Element queue => doubly-linked list */
    protected volatile BoundedQElementContainer prev = null;

    /** Reuse Counter */
    protected int reuseCounter = 0;

    /** Next element in queue */
    public final AtomicReference<BoundedQElementContainer> next2 = new AtomicReference<BoundedQElementContainer>(getDummy(0));

    protected BoundedQElementContainer() {
        registerForIndex(); // bounded queue requires containers to be registered
    }

    /**
     * (Should be overridden by subclass - which should in turn call recycle())
     *
     * @param recycleContent Also recycle element in container?
     */
    abstract protected void recycle(boolean recycleContent);

    /**
     * Recycle only contents of this container
     */
    abstract protected void recycleContent();

    /**
     * Recycle content of possibly another container
     * (pseudo-static method - should do the same as anoterContainer->recycleContent())
     *
     * @param content Content to recycle
     */
    abstract public void recycleContent(Object content);


    protected void recycle() {
        assert(!isDummy());
        reuseCounter++;
        assert(stateChange(Reusable.ENQUEUED, Reusable.POST_QUEUED, owner));
//      assert(!recycled || RuntimeEnvironment.shuttingDown);
//      recycled = true;
        next2.set(getDummy(reuseCounter));
        prev = null;
        assert(next2.get().isDummy());
        super.recycle();
    }

    public boolean isDummy() {
        return element == DUMMY_MARKER;
    }

    /**
     * Static initialization. May be done before everything else.
     */
    public static void staticInit() {
        if (initialized) {
            return;
        }
        assert(dummies == null);
        dummies = new BoundedQElementContainer[DUMMY_ELEMENTS];
        for (int i = 0; i < DUMMY_ELEMENTS; i++) {
            BoundedQElementContainer tmp = new Dummy();
            tmp.next2.set(getDummy(0));
            dummies[i] = tmp;
        }
        initialized = true;
    }

    /**
     * Get Dummy element for specified index
     */
    protected static BoundedQElementContainer getDummy(int index) {
        return dummies[index & DUMMY_MASK];
    }
}

/**
 * @author Max Reichardt
 *
 * Dummy element for safely invalidating next2 AtomicPtr
 */
class Dummy extends BoundedQElementContainer {

    public Dummy() {
        element = BoundedQElementContainer.DUMMY_MARKER;
    }

    @Override
    protected void recycle(boolean recycleContent) {
        throw new RuntimeException("Dummy containers may not be recycled");
    }

    @Override
    protected void recycleContent() {
        throw new RuntimeException("Dummy container content may not be recycled");
    }

    @Override
    public void recycleContent(Object content) {
        throw new RuntimeException("Dummy container content may not be recycled (2)");
    }
}
