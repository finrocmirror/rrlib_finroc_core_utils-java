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

import org.finroc.jc.annotation.Attribute;
import org.finroc.jc.annotation.Const;
import org.finroc.jc.annotation.CppDefault;
import org.finroc.jc.annotation.InCpp;
import org.finroc.jc.annotation.Include;
import org.finroc.jc.annotation.IncludeClass;
import org.finroc.jc.annotation.Inline;
import org.finroc.jc.annotation.JavaOnly;
import org.finroc.jc.annotation.NoCpp;
import org.finroc.jc.annotation.PassByValue;
import org.finroc.jc.annotation.Ptr;
import org.finroc.xml.XMLNode;

/**
 * @author max
 *
 * Used for initially creating/instantiating GenericObject.
 */
@Inline @NoCpp @Include("clear.h")
@IncludeClass( {StringInputStream.class, StringOutputStream.class})
public class GenericObjectInstance <T extends RRLibSerializable, M extends GenericObjectManager> extends GenericObject {

    /** Manager */
    @SuppressWarnings("unused")
    @Attribute("aligned(8)")
    @PassByValue private final M manager;

    /** Instantiated data */
    @Attribute("aligned(8)")
    @PassByValue private final T data;

    /**
     * @param wrappedObject Wrapped object
     * @param dt Data type of wrapped object
     */
    @JavaOnly
    public GenericObjectInstance(T wrappedObject, DataTypeBase dt, M manager) {
        super(dt);
        data = wrappedObject;
        wrapped = data;
        this.jmanager = manager;
        this.manager = null;
    }

    /*Cpp
    GenericObjectInstance() : GenericObject(DataType<T>()), manager(), data() {
        assert((reinterpret_cast<char*>(&manager) - reinterpret_cast<char*>(this)) == MANAGER_OFFSET && "Manager offset invalid");
        wrapped = &data;
    }
     */

    @Override
    @InCpp("os << data;")
    public void serialize(OutputStreamBuffer os) {
        getData().serialize(os);
    }

    @Override
    @InCpp("is >> data;")
    public void deserialize(InputStreamBuffer is) {
        getData().deserialize(is);
    }

    @Override
    @InCpp("os << data;")
    public void serialize(StringOutputStream os) {
        getData().serialize(os);
    }

    @Override
    @InCpp("is >> data;")
    public void deserialize(StringInputStream is) throws Exception {
        getData().deserialize(is);
    }

    @Override
    @InCpp("node << data;")
    public void serialize(XMLNode node) throws Exception {
        getData().serialize(node);
    }

    @Override
    @InCpp("node >> data;")
    public void deserialize(XMLNode node) throws Exception {
        getData().deserialize(node);
    }

    @SuppressWarnings("unchecked")
    @Override
    @InCpp("deepCopyFromImpl(*static_cast<const T*>(source), f);")
    protected void deepCopyFrom(Object source, @CppDefault("NULL") @Ptr Factory f) {
        deepCopyFromImpl((T)source, f);
    }

    /**
     * Deep copy source object to this object
     *
     * @param source Source object
     */
    public void deepCopyFromImpl(@Const T source, @CppDefault("NULL") @Ptr Factory f) {
        Serialization.deepCopy(source, data, f);
    }

    @InCpp("clear::clear(&data);")
    @Override
    public void clear() {
        if (data instanceof Clearable) {
            ((Clearable)data).clearObject();
        }
    }
}
