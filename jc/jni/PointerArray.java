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
package org.rrlib.finroc_core_utils.jc.jni;

import java.util.List;

/**
 * @author Max Reichardt
 *
 * Array of Pointers in C++
 */
public class PointerArray extends SharedBuffer {

    public PointerArray(List<UsedInC> objectsWithPointers) {
        super(objectsWithPointers.size() * JNIInfo.getPointerSize());
        for (int i = 0; i < objectsWithPointers.size(); i++) {
            JNICalls.setPointer(getPointer(), i, objectsWithPointers.get(i).getPointer());
            ownedObjects.add(objectsWithPointers.get(i));
        }
    }

    public PointerArray(int size) {
        super(size);
    }

    public PointerArray(long pointer) {
        super(pointer);
    }

    public long getPointer(int index) {
        return JNICalls.getPointer(getPointer(), index);
    }
}
