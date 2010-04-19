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
package org.finroc.jc.jni;

import java.util.ArrayList;
import java.util.List;

import org.finroc.jc.annotation.JavaOnly;

/**
 * @author max
 *
 * Marks all objects wrapping something that can also be used in C++
 */
@JavaOnly
public abstract class UsedInC {

    /** Objects and buffers that need to exist in C++ as long as this object does */
    protected List<Object> ownedObjects = new ArrayList<Object>();

    /** get Pointer of object on C side */
    public abstract long getPointer();

    public void addOwnedObject(Object uic) {
        ownedObjects.add(uic);
    }
}
