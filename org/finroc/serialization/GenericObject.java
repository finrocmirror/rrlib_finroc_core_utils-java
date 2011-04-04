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
package org.finroc.serialization;

import org.finroc.jc.annotation.Const;
import org.finroc.jc.annotation.ConstMethod;
import org.finroc.jc.annotation.CppDefault;
import org.finroc.jc.annotation.InCpp;
import org.finroc.jc.annotation.Inline;
import org.finroc.jc.annotation.JavaOnly;
import org.finroc.jc.annotation.NoCpp;
import org.finroc.jc.annotation.NonVirtual;
import org.finroc.jc.annotation.Ptr;
import org.finroc.jc.annotation.VoidPtr;

/**
 * @author max
 *
 * Container/wrapper for an arbitrary object.
 *
 * Provides base functionality such as deep copying, type information
 * and serialization.
 * It also assert that casting back is only possible to the original type.
 *
 * This allows to handle objects in a uniform way.
 *
 * Memory Layout of all subclasses: vtable ptr | datatype ptr | object ptr | management info raw memory of size M
 */
@Inline @NoCpp
public abstract class GenericObject extends TypedObjectImpl {

    /** Wrapped object */
    protected @VoidPtr Object wrapped;

    /** Management information for this generic object. */
    @JavaOnly protected GenericObjectManager jmanager;

    //Cpp static const size_t MANAGER_OFFSET = (sizeof(void*) == 4) ? 16 : 24;

    /**
     * @param wrapped Wrapped object
     * @param dt Data Type of wrapped object
     */
    GenericObject(DataTypeBase dt) {
        type = dt;
    }

    /*Cpp
    template <typename T>
    const T* getData() const {
        assert(typeid(T).name() == type.getRttiName());
        return static_cast<const T*>(wrapped);
    }
     */

    /**
     * @return Wrapped object (type T must match original type)
     */
    @SuppressWarnings("unchecked")
    @Ptr @Inline public <T extends RRLibSerializable> T getData() {

        //JavaOnlyBlock
        return (T)wrapped;

        //Cpp assert(typeid(T).name() == type.getRttiName());
        //Cpp return static_cast<T*>(wrapped);
    }

    /**
     * Raw void pointer to wrapped object
     */
    @ConstMethod public @Const @VoidPtr Object getRawDataPtr() {
        return wrapped;
    }

    /**
     * Deep copy source object to this object
     * (types MUST match)
     *
     * @param source Source object
     */
    @Inline
    public void deepCopyFrom(@Const @Ptr GenericObject source, @CppDefault("NULL") @Ptr Factory f) {
        assert(source.type == type) : "Types must match";

        //JavaOnlyBlock
        deepCopyFrom((Object)source.wrapped, f);

        //Cpp deepCopyFrom(source->wrapped, f);
    }

    /*Cpp
    #if __SIZEOF_POINTER__ == 4
    int fill_dummy; // fill 4 byte to ensure that managers are 8-byte-aligned on 32 bit platforms
    #endif
     */

    /**
     * Deep copy source object to this object
     * (types MUST match)
     *
     * @param source Source object
     */
    protected abstract void deepCopyFrom(@Const @VoidPtr Object source, @CppDefault("NULL") @Ptr Factory f);

    /**
     * @return Management information for this generic object.
     */
    @NonVirtual
    @InCpp("return reinterpret_cast<GenericObjectManager*>(reinterpret_cast<char*>(this) + MANAGER_OFFSET);")
    public GenericObjectManager getManager() {
        return jmanager;
    }

    /**
     * Clear any shared resources that this object holds on to
     * (e.g. for reusing object in pool)
     */
    public abstract void clear();

}