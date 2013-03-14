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
import org.rrlib.finroc_core_utils.serialization.StringInputStream;
import org.rrlib.finroc_core_utils.serialization.StringOutputStream;
import org.rrlib.finroc_core_utils.xml.XMLNode;

/**
 * @author Max Reichardt
 *
 * Allows wrapping any object as GenericObject
 */
public class GenericObjectBaseImpl <T extends RRLibSerializable> extends GenericObject {

    /**
     * @param wrappedObject Wrapped object
     * @param dt Data type of wrapped object
     */
    public GenericObjectBaseImpl(T wrappedObject, DataTypeBase dt) {
        super(dt);
        wrapped = wrappedObject;
    }

    @Override
    public void serialize(OutputStreamBuffer os) {
        getData().serialize(os);
    }

    @Override
    public void deserialize(InputStreamBuffer is) {
        getData().deserialize(is);
    }

    @Override
    public void serialize(StringOutputStream os) {
        getData().serialize(os);
    }

    @Override
    public void deserialize(StringInputStream is) throws Exception {
        getData().deserialize(is);
    }

    @Override
    public void serialize(XMLNode node) throws Exception {
        getData().serialize(node);
    }

    @Override
    public void deserialize(XMLNode node) throws Exception {
        getData().deserialize(node);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void deepCopyFrom(Object source, Factory f) {
        deepCopyFromImpl((T)source, f);
    }

    /**
     * Deep copy source object to this object
     *
     * @param source Source object
     */
    public void deepCopyFromImpl(T source, Factory f) {
        Serialization.deepCopy(source, getData(), f);
    }

    @Override
    public void clear() {
        if (getData() instanceof Clearable) {
            ((Clearable)getData()).clearObject();
        }
    }
}
