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

import org.rrlib.finroc_core_utils.jc.ArrayWrapper;
import org.rrlib.finroc_core_utils.jc.MutexLockOrder;
import org.rrlib.finroc_core_utils.jc.annotation.Const;
import org.rrlib.finroc_core_utils.jc.annotation.ConstMethod;
import org.rrlib.finroc_core_utils.jc.annotation.CppInclude;
import org.rrlib.finroc_core_utils.jc.annotation.HAppend;
import org.rrlib.finroc_core_utils.jc.annotation.InCpp;
import org.rrlib.finroc_core_utils.jc.annotation.InCppFile;
import org.rrlib.finroc_core_utils.jc.annotation.Init;
import org.rrlib.finroc_core_utils.jc.annotation.JavaOnly;
import org.rrlib.finroc_core_utils.jc.annotation.Ptr;
import org.rrlib.finroc_core_utils.jc.annotation.Ref;
import org.rrlib.finroc_core_utils.jc.annotation.SizeT;

/**
 * @author max
 *
 * Implementation of class below.
 */
@CppInclude("GarbageCollector.h")
@HAppend( {
    "//RESIZEFACTOR: Factor by which list is enlarged, when backend is too small",
    "//DELETEELEMSWITHLIST: Delete elements when List is deleted? (relevant for C++ only)",
    "template <typename T, size_t _RESIZE_FACTOR = 4, bool _DELETE_ELEMS_WITH_LIST = false>",
    "class SafeConcurrentlyIterableList : public SafeConcurrentlyIterableListBase<T> {",
    "",
    "public:",
    "	// initialSize: Initial size of first backend",
    "	// resizeFactor: Dummy in C++ - retained for Java compatibility",
    "	SafeConcurrentlyIterableList(size_t initialSize, size_t resizeFactor = -1) :",
    "			SafeConcurrentlyIterableListBase<T>(initialSize) {}",
    "",
    "   virtual ~SafeConcurrentlyIterableList() {",
    "       clearAndDelete();",
    "   }",
    "",
    "   virtual void clearAndDelete() {",
    "       ArrayWrapper<T>* iterable = this->getIterable();",
    "       for (size_t i = 0, n = iterable->size(); i < n; i++) {",
    "           GarbageCollector::deleteDeferred(iterable->get(i));",
    "       }",
    "       this->clear();",
    "   }",
    "",
    "protected:",
    "",
    "	virtual size_t getResizeFactor() {",
    "		return _RESIZE_FACTOR;",
    "	}",
    "",
    "	virtual bool deleteElemsWithList() {",
    "		return _DELETE_ELEMS_WITH_LIST;",
    "	}",
    "",
    "};",
    "",
    "//RESIZEFACTOR: Factor by which list is enlarged, when backend is too small",
    "//DELETEELEMSWITHLIST: Delete elements when List is deleted? (relevant for C++ only)",
    "template <typename T, size_t _RESIZE_FACTOR>",
    "class SafeConcurrentlyIterableList<T, _RESIZE_FACTOR, false> : public SafeConcurrentlyIterableListBase<T> {",
    "",
    "public:",
    "   // initialSize: Initial size of first backend",
    "   // resizeFactor: Dummy in C++ - retained for Java compatibility",
    "   SafeConcurrentlyIterableList(size_t initialSize, size_t resizeFactor = -1) :",
    "           SafeConcurrentlyIterableListBase<T>(initialSize) {}",
    "",
    "   virtual ~SafeConcurrentlyIterableList() {",
    "       this->clear();",
    "   }",
    "",
    "protected:",
    "",
    "   virtual size_t getResizeFactor() {",
    "       return _RESIZE_FACTOR;",
    "   }",
    "",
    "   virtual bool deleteElemsWithList() {",
    "       return false;",
    "   }",
    "",
    "};"
})

abstract class SafeConcurrentlyIterableListBase<T> {

    /*Cpp
    // Will store old arrays until object is deleted
    //AutoDeleter autoDeleter;
    */

    /** Mutex for list - Since we call garbage collector lock for list needs to be before in order */
    public final MutexLockOrder objMutex;

    /** Current list backend */
    protected volatile @Ptr ArrayWrapper<T> currentBackend;

    /** optimization variable: There are no free entries before this index */
    @SizeT private int firstFreeFromHere = 0;

    /**
     * @param initialSize Initial size of backend
     * @param resizeFactor Factor by which list is enlarged, when backend is too small (dummy in C++, template parameter specifies it here)
     * @param deleteElemsWithList Delete elements when List is deleted? (relevant for C++ only)
     */
    @SuppressWarnings("unchecked")
    @Init( {"currentBackend(initialSize > 0 ? new ArrayWrapper<T>((size_t)0, initialSize) : &(ArrayWrapper<T>::getEmpty()))"})
    public SafeConcurrentlyIterableListBase(@SizeT int initialSize) {
        objMutex = new MutexLockOrder(Integer.MAX_VALUE - 20);
        currentBackend = initialSize > 0 ? new ArrayWrapper<T>(0, initialSize) : ArrayWrapper.getEmpty();
    }

    /*Cpp
    virtual ~SafeConcurrentlyIterableListBase() {
        deleteBackend(currentBackend); // clear() is done in non-abstract subclass
    }
     */

    /**
     * Add element
     *
     * @param element element
     * @param appendToBack Append new element to back O(1)? (or rather search for hole => O(n), but possibly smaller list and faster iteration)
     */
    public synchronized void add(@Const T element, boolean appendToBack) {
        @Ptr ArrayWrapper<T> backend = currentBackend; // acquire non-volatile pointer
        if (!appendToBack) {
            for (int i = firstFreeFromHere, n = backend.size(); i < n; i++) {
                if (backend.get(i) == getNullElement()) {
                    backend.set(i, element);
                    firstFreeFromHere = i + 1;
                    return;
                }
            }
            firstFreeFromHere = backend.size() + 1;
        }
        if (backend.freeCapacity()) {
            backend.add(element);
        } else {
            @Ptr ArrayWrapper<T> old = currentBackend;
            @InCpp("ArrayWrapper<T>* newBackend = new ArrayWrapper<T>((size_t)0, std::_max<size_t>(1, backend->getCapacity()) * getResizeFactor());")
            @Ptr ArrayWrapper<T> newBackend = new ArrayWrapper<T>(0, Math.max(1, backend.getCapacity()) * getResizeFactor());
            newBackend.copyAllFrom(backend);
            newBackend.add(element);
            currentBackend = newBackend;

            if (old.size() > 0) { // we don't want to delete empty backend from ArrayWrapper class
                deleteBackend(old);
            }
        }
    }

    @InCppFile
    private void deleteBackend(@Ptr ArrayWrapper<T> b) {
        /*Cpp
        if (b != &(ArrayWrapper<T>::getEmpty())) {
            GarbageCollector::deleteDeferred(b);
        }
        */
    }

    /**
     * @return Null/empty element (marks free slots in array)
     */
    @InCpp("return (T)NULL;")
    @ConstMethod private T getNullElement() {
        return (T)null;
    }

    /**
     * @return Factor by which list is enlarged, when backend is too small
     */
    protected abstract @SizeT int getResizeFactor();

    /**
     * @return Delete elements when List is deleted? (relevant for C++ only)
     */
    protected abstract boolean deleteElemsWithList();

    /**
     * Ensure/Reserve specified capacity
     *
     * @param cap Capacity
     */
    public synchronized void ensureCapacity(@SizeT int cap) {
        @Ptr ArrayWrapper<T> backend = currentBackend; // acquire non-volatile pointer
        if (backend.getCapacity() < cap) {
            @Ptr ArrayWrapper<T> old = currentBackend;
            @Ptr ArrayWrapper<T> newBackend = new ArrayWrapper<T>(0, cap * getResizeFactor());
            newBackend.copyAllFrom(backend);
            currentBackend = newBackend;

            deleteBackend(old);
        }
    }

    /**
     * @return Safe View for iterating over list
     */
    @ConstMethod public @Ptr ArrayWrapper<T> getIterable() {
        return currentBackend;
    }

    /**
     * Remove element
     *
     * @param element Element to remove
     */
    public synchronized void remove(@Const T element) {
        @Ptr ArrayWrapper<T> iterable = getIterable();
        for (@SizeT int i = 0, n = iterable.size(); i < n; i++) {
            if (iterable.get(i) == element) {

                /*Cpp
                if (deleteElemsWithList()) {
                    GarbageCollector::deleteDeferred(iterable->get(i));
                    //delete iterable.get(i);
                }
                */

                iterable.set(i, getNullElement());
                firstFreeFromHere = Math.min(firstFreeFromHere, i);

                // Shrink list when last elements are deleted
                if (i == size() - 1) {
                    iterable.setSize(size() - 1);

                    while (size() > 0 && iterable.get(size() - 1) == getNullElement()) {
                        iterable.setSize(size() - 1);
                    }
                }

                break;
            }
        }
    }

    /**
     * @return Returns size of list
     */
    @ConstMethod public @SizeT int size() {
        return getIterable().size();
    }

    /**
     * @return Returns number of elements currently in list (in parallel - so can already be invalid due to concurrent modifications)
     */
    @ConstMethod public @SizeT int countElements() {
        @Ptr ArrayWrapper<T> iterable = getIterable();
        int count = 0;
        for (@SizeT int i = 0, n = iterable.size(); i < n; i++) {
            if (iterable.get(i) != getNullElement()) {
                count++;
            }
        }
        return count;
    }

    /**
     * @return Clear list
     */
    public synchronized void clear() {
        getIterable().clear();
        firstFreeFromHere = 0;
    }

    /**
     * Are any non-null elements in current backend?
     */
    public boolean isEmpty() {
        @Ptr ArrayWrapper<T> iterable = getIterable();
        for (@SizeT int i = 0, n = iterable.size(); i < n; i++) {
            if (iterable.get(i) != getNullElement()) {
                return false;
            }
        }
        return true;
    }
}

/**
 * @author max
 *
 * This list is thread-safe. It can be iterated over (concurrently to modifications)
 * by many threads without blocking. Iterations are very efficient (both C++ and Java).
 * The list may contain null entries after deleting (so check!).
 *
 * To efficiently iterate over the list, this code should be used:
 *
 *      Java:
 *       ArrayWrapper<T> iterable = xyz.getIterable();
 *       for (int i = 0, n = iterable.size(); i < n; i++) {
 *          T x = iterable.get(i);
 *          if (x != null) {
 *              // do something
 *          }
 *       }
 *
 *      C++:
 *       ArrayWrapper<T>* iterable = xyz.getIterable();
 *       for (int i = 0, n = iterable->size(); i < n; i++) {
 *          T* x = iterable->get(i);
 *          if (x != NULL {
 *              // do something
 *          }
 *       }
 *
 * Idea: Unlike ArrayList (Java) or std::vector (C++), old Array backends are deleted deferred so that
 * threads still iterating over this area can always complete this.
 * TODO: If iterations can be particularly delayed, use a delay-iterator in C++.
 *
 *
 * (C++) This list may only contain pointers (no shared pointers etc. because of thread-safety)...
 *       because only this allows atomic deletion by overwriting with null
 */
@JavaOnly /*@CppDelegate(SafeConcurrentlyIterableListBase.class)*/
public class SafeConcurrentlyIterableList<T> extends SafeConcurrentlyIterableListBase<T> {

    /** Empty Dummy List */
    @SuppressWarnings("rawtypes")
    private static final SafeConcurrentlyIterableList EMPTY = new SafeConcurrentlyIterableList(0, 0);

    /** Factor by which list is enlarged, when backend is too small */
    private final @SizeT int resizeFactor;

    /**
     * @return Empty Dummy List
     */
    @SuppressWarnings("rawtypes")
    @Ref public static SafeConcurrentlyIterableList getEmptyInstance() {
        return EMPTY;
    }

    /**
     * @param initialSize Initial size of backend
     * @param resizeFactor Factor by which list is enlarged, when backend is too small (dummy in C++, template parameter specifies it here)
     */
    public SafeConcurrentlyIterableList(@SizeT int initialSize, @SizeT int resizeFactor_) {
        super(initialSize);
        resizeFactor = resizeFactor_;
    }

    @Override
    protected boolean deleteElemsWithList() {
        return false;
    }

    @Override
    protected int getResizeFactor() {
        return resizeFactor;
    }
}
