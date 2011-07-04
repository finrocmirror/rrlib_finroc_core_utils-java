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

import org.rrlib.finroc_core_utils.jc.annotation.Attribute;
import org.rrlib.finroc_core_utils.jc.annotation.Include;
import org.rrlib.finroc_core_utils.jc.annotation.IncludeClass;
import org.rrlib.finroc_core_utils.jc.annotation.Inline;
import org.rrlib.finroc_core_utils.jc.annotation.JavaOnly;
import org.rrlib.finroc_core_utils.jc.annotation.NoCpp;
import org.rrlib.finroc_core_utils.jc.annotation.PassByValue;
import org.rrlib.finroc_core_utils.jc.annotation.Ptr;
import org.rrlib.finroc_core_utils.jc.annotation.SkipArgs;

/**
 * @author max
 *
 * Allows wrapping any object as GenericObject
 */
@Inline @NoCpp @Include("clear.h")
@IncludeClass( {StringInputStream.class, StringOutputStream.class, GenericObjectManager.class})
public class GenericObjectWrapper <T extends RRLibSerializable, M extends GenericObjectManager> extends GenericObjectBaseImpl<T> {

    /** Manager */
    @SuppressWarnings("unused")
    @Attribute("aligned(8)")
    @PassByValue private final M manager;

    /**
     * @param wrappedObject Wrapped object
     * @param dt Data type of wrapped object
     */
    @JavaOnly @SkipArgs("2")
    public GenericObjectWrapper(@Ptr T wrappedObject, DataTypeBase dt, M manager) {
        super(wrappedObject, dt);
        this.jmanager = manager;
        this.manager = null;
    }

    /*Cpp
    public:
    GenericObjectWrapper(T* wrappedObject) : GenericObjectBaseImpl<T>(), manager() {
        assert((reinterpret_cast<char*>(&manager) - reinterpret_cast<char*>(this)) == this->MANAGER_OFFSET && "Manager offset invalid");
        this->wrapped = wrappedObject;
    }
     */
}
