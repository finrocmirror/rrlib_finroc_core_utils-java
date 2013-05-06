//
// You received this file as part of RRLib
// Robotics Research Library
//
// Copyright (C) Finroc GbR (finroc.org)
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//
//----------------------------------------------------------------------
package org.rrlib.finroc_core_utils.jc;

/**
 * @author Max Reichardt
 *
 * All objects added to this container will be deleted as soon as the container is.
 */
public class AutoDeleter {

    private static AutoDeleter instance = new AutoDeleter();

    /**
     * Registers object/resource for deletion when this object is deleted
     * (irrelevant for Java)
     *
     * @param del (Pointer to) object to delete with this object
     */
    public void add(Object del) {}

    public static AutoDeleter getStaticInstance() {
        return instance;
    }

    /**
     * Registers object/resource for deletion when program ends
     * (irrelevant for Java)
     *
     * @param del (Pointer to) object to delete when program ends
     */
    private static void addStaticImpl(Object del) {
        getStaticInstance().add(del);
    }

    /**
     * Registers object/resource for deletion when program ends
     * (irrelevant for Java)
     *
     * @param del (Pointer to) object to delete when program ends
     */
    public static <T> T addStatic(T obj) {
        addStaticImpl(obj);
        return obj;
    }
}
