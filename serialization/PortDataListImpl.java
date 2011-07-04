/**
 * You received this file as part of an advanced experimental
 * robotics framework prototype ('finroc')
 *
 * Copyright (C) 2010-2011 Max Reichardt,
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

import java.util.ArrayList;

import org.rrlib.finroc_core_utils.jc.annotation.JavaOnly;

/**
 * @author max
 *
 * Standard Implementation of PortDataList interface.
 */
@JavaOnly
public class PortDataListImpl<T extends RRLibSerializable> extends RRLibSerializableImpl implements PortDataList<T> {

    /** Wrapped list */
    private ArrayList<T> wrapped = new ArrayList<T>();

    /** Data type of list elements */
    private DataTypeBase elementType;

    public PortDataListImpl(DataTypeBase type) {
        elementType = type;
    }

    /** for cloner implementation */
    @Deprecated
    public PortDataListImpl() {
    }

    @Override
    public void serialize(OutputStreamBuffer os) {
        os.writeInt(wrapped.size());
        boolean constType = true;
        for (int i = 0; i < wrapped.size(); i++) {
            constType &= wrapped.get(i).getClass().equals(elementType.getJavaClass());
        }
        os.writeBoolean(constType);
        for (int i = 0; i < wrapped.size(); i++) {
            if (!constType) {
                if (wrapped.get(i) == null) {
                    os.writeType(DataTypeBase.getNullType());
                    continue;
                }
                os.writeType(DataTypeBase.findType(wrapped.get(i).getClass()));
            }
            wrapped.get(i).serialize(os);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void deserialize(InputStreamBuffer is) {
        int size = is.readInt();
        boolean constType = is.readBoolean();
        for (int i = 0; i < size; i++) {
            DataTypeBase type = elementType;
            if (!constType) {
                type = is.readType();
                if (type == DataTypeBase.getNullType()) {
                    wrapped.set(i, null);
                    continue;
                }
            }
            if (i < wrapped.size() && wrapped.get(i).getClass().equals(type.getJavaClass())) {
                ((T)wrapped.get(i)).deserialize(is);
            } else {
                try {
                    T s = (T)type.createInstance();
                    s.deserialize(is);
                    if (i >= wrapped.size()) {
                        wrapped.add(s);
                    } else {
                        wrapped.set(i, s);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        }

        // delete elements that are too much
        while (wrapped.size() > size) {
            wrapped.remove(wrapped.size() - 1);
        }
    }

    @Override
    public void copyFrom(PortDataList<T> source) {
        if (elementType == null) {
            elementType = ((PortDataListImpl<T>)source).elementType;
        }
        Serialization.deepCopy(source, this, null);
    }

    @Override
    public int size() {
        return wrapped.size();
    }

    @Override
    public T get(int index) {
        return wrapped.get(index);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void resize(int newSize) {
        while (newSize < wrapped.size()) {
            wrapped.remove(wrapped.size() - 1);
        }
        while (newSize > wrapped.size()) {
            wrapped.add((T)elementType.createInstance());
        }
    }

    @SuppressWarnings( { "unchecked", "rawtypes" })
    @Override
    public void applyChange(PortDataList<T> t, long offset, long parameter2) {
        for (long i = offset; i < Math.min(offset + t.size(), size()); i++) {
            if (wrapped.get((int)i) instanceof GenericChangeable) {
                ((GenericChangeable)wrapped.get((int)i)).applyChange(t.get((int)(i - offset)), parameter2, 0);
            } else {
                wrapped.set((int)i, t.get((int)(i - offset)));
            }
        }
    }
}