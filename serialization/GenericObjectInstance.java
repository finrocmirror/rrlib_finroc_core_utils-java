/**
 * You received this file as part of RRLib serialization
 *
 * Copyright (C) 2011 Max Reichardt,
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
package org.rrlib.finroc_core_utils.serialization;

import org.rrlib.finroc_core_utils.jc.annotation.Include;
import org.rrlib.finroc_core_utils.jc.annotation.IncludeClass;
import org.rrlib.finroc_core_utils.jc.annotation.Inline;
import org.rrlib.finroc_core_utils.jc.annotation.JavaOnly;
import org.rrlib.finroc_core_utils.jc.annotation.NoCpp;

/**
 * @author max
 *
 * Used for initially creating/instantiating GenericObject.
 *
 * This class should only be instantiated by tDataType !
 */
@Inline @NoCpp @Include("clear.h")
@IncludeClass( {StringInputStream.class, StringOutputStream.class})
public class GenericObjectInstance <T extends RRLibSerializable> extends GenericObjectBaseImpl<T> {

    /**
     * @param wrappedObject Wrapped object
     * @param dt Data type of wrapped object
     */
    @JavaOnly
    public GenericObjectInstance(T wrappedObject, DataTypeBase dt, GenericObjectManager manager) {
        super(wrappedObject, dt);
        this.jmanager = manager;
    }

    /*Cpp
    public:
    GenericObjectInstance(T* wrappedObject) : GenericObjectBaseImpl<T>() {
        this->wrapped = wrappedObject;
    }

    virtual ~GenericObjectInstance() {
        T* t = GenericObject::getData<T>();
        t->~T();
    }
     */
}