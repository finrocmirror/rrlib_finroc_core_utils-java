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
package org.rrlib.finroc_core_utils.rtti;

import org.rrlib.finroc_core_utils.serialization.InputStreamBuffer;
import org.rrlib.finroc_core_utils.serialization.OutputStreamBuffer;
import org.rrlib.finroc_core_utils.serialization.RRLibSerializable;
import org.rrlib.finroc_core_utils.serialization.Serialization;
import org.rrlib.finroc_core_utils.serialization.Serialization.DataEncoding;

/**
 * @author Max Reichardt
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
public abstract class GenericObject extends TypedObjectImpl {

    /** Wrapped object */
    protected Object wrapped;

    /** Management information for this generic object. */
    protected GenericObjectManager jmanager;

    /**
     * @param wrapped Wrapped object
     * @param dt Data Type of wrapped object
     */
    GenericObject(DataTypeBase dt) {
        type = dt;
    }

    /**
     * @return Wrapped object (type T must match original type)
     */
    @SuppressWarnings("unchecked")
    public <T extends RRLibSerializable> T getData() {
        return (T)wrapped;
    }

    /**
     * Raw void pointer to wrapped object
     */
    public Object getRawDataPtr() {
        return wrapped;
    }

    /**
     * Deep copy source object to this object
     * (types MUST match)
     *
     * @param source Source object
     */
    @SuppressWarnings( { "unchecked", "rawtypes" })
    public void deepCopyFrom(GenericObject source, Factory f) {
        if (source.type == type) {
            deepCopyFrom((Object)source.wrapped, f);
        } else if (Copyable.class.isAssignableFrom(type.getJavaClass())) {
            ((Copyable)wrapped).copyFrom(source.wrapped);
        } else {
            throw new RuntimeException("Types must match");
        }
    }

    /**
     * Deep copy source object to this object
     * (types MUST match)
     *
     * @param source Source object
     */
    protected abstract void deepCopyFrom(Object source, Factory f);

    /**
     * @return Management information for this generic object.
     */
    public GenericObjectManager getManager() {
        return jmanager;
    }

    /**
     * Clear any shared resources that this object holds on to
     * (e.g. for reusing object in pool)
     */
    public abstract void clear();

    /**
     * Deserialize data from binary input stream - possibly using non-binary encoding.
     *
     * @param is Binary input stream
     * @param enc Encoding to use
     */
    public void deserialize(InputStreamBuffer is, DataEncoding enc) {
        Serialization.deserialize(is, getData(), enc);
    }

    /**
     * Serialize data to binary output stream - possibly using non-binary encoding.
     *
     * @param os Binary output stream
     * @param enc Encoding to use
     */
    public void serialize(OutputStreamBuffer os, DataEncoding enc) {
        Serialization.serialize(os, getData(), enc);
    }

}
