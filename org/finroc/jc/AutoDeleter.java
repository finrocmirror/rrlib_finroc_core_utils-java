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

import org.finroc.jc.annotation.CppType;
import org.finroc.jc.annotation.InCpp;
import org.finroc.jc.annotation.Include;
import org.finroc.jc.annotation.Managed;
import org.finroc.jc.annotation.Ptr;
import org.finroc.jc.annotation.SharedPtr;
import org.finroc.jc.container.AllocationRegister;

/**
 * @author max
 *
 * All objects added to this container will be deleted as soon as the container is.
 */
@Include("<vector>")
public class AutoDeleter {

    /*Cpp
    std::vector<SafeDestructible*> deletables;
     */

    /** "Lock" to allocation register - ensures that report will be printed after static auto-deleter has been deleted */
    @SuppressWarnings("unused")
    private static AllocationRegister allocationRegisterLock = AllocationRegister.getInstance();

    /** Singleton static instance for all static objects that should be deleted, when the program finishes */
    @SharedPtr private static AutoDeleter instance;

    /*Cpp
    virtual ~AutoDeleter() {
        Thread::stopThreads();

        // delete in reverse order
        for (int i = ((int)deletables._size()) - 1; i >= 0; i--) {
            deletables[i]->autoDelete();
        }
    }

    inline void add(SafeDestructible* del) {
        deletables.push_back(del);
    }
     */

    /**
     * Registers object/resource for deletion when this object is deleted
     * (irrelevant for Java)
     *
     * @param del (Pointer to) object to delete with this object
     */
    @InCpp("deletables.push_back(del);")
    public void add(@Ptr Object del) {}

    /*Cpp
    // same as below - helps with multiple inheritance problem
    inline static void addStaticImpl(Object* del) {
        addStatic(static_cast<SafeDestructible*>(del));
    }
     */

    /**
     * Registers object/resource for deletion when program ends
     * (irrelevant for Java)
     *
     * @param del (Pointer to) object to delete when program ends
     */
    private static void addStaticImpl(@Ptr @CppType("SafeDestructible") Object del) {
        if (instance == null) {
            instance = new AutoDeleter(); // usually called during static initialization - so no problems with concurrency
        }
        //Cpp instance->add(del);
    }

    /**
     * Registers object/resource for deletion when program ends
     * (irrelevant for Java)
     *
     * @param del (Pointer to) object to delete when program ends
     */
    public static @Ptr <T> T addStatic(@Managed @Ptr T obj) {
        addStaticImpl(obj);
        return obj;
    }
}
