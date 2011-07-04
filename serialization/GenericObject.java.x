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
import org.finroc.jc.annotation.CppType;
import org.finroc.jc.annotation.Inline;
import org.finroc.jc.annotation.JavaOnly;
import org.finroc.jc.annotation.NoCpp;
import org.finroc.jc.annotation.Ptr;
import org.finroc.jc.annotation.VoidPtr;

/**
 * @author max
 *
 * Container for an arbitrary object.
 * 
 * Provides base functionality such as deep copying, type information
 * and serialization.
 * It also assert that casting back is only possible to the original type.
 * 
 * This allows to handle objects in a uniform way.
 */
@Inline @NoCpp
public abstract class GenericObject implements RRLibSerializable {

    /** Data Type of wrapped object */
    private final DataTypeBase type;
    
    /** Management information for generic object */
    @JavaOnly private final GenericObjectManager management;
    
    /**
     * @param wrapped Wrapped object
     * @param dt Data Type of wrapped object
     */
    GenericObject(DataTypeBase dt, GenericObjectManager management) {
        this.type = dt;
        this.management = management;
    }
    
    /**
     * @return Data Type of wrapped object
     */
    @ConstMethod public DataTypeBase getType() {
        return type;
    }
    
    /**
     * @return Wrapped object (type T must match original type)
     */
    @SuppressWarnings("unchecked")
    @ConstMethod @Const @Ptr public <T extends RRLibSerializable> T getData() {
        return (T)getDataImpl(null);
        
        //Cpp return static_cast<const T*>(getDataImpl(typeid(T).name()));
    }
    
    /**
     * @param typeid Typeid that must match the one of original type (C++ only)
     * @return Wrapped object
     */
    @ConstMethod public abstract @Const @VoidPtr Object getDataImpl(@CppType("char*") String typeid);

    /*Cpp
    template <typename T>
    T* getData() {
        return static_cast<T*>(getDataImpl(typeid(T).name()));
    }
    
    void* getDataImpl(const char* typeid) = 0;
     */
    
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
        deepCopyFrom(getDataImpl(null), f);
        
        //Cpp deepCopyFrom(source->getDataImpl, f);
    }
    
    /**
     * Deep copy source object to this object
     * (types MUST match)
     * 
     * @param source Source object
     */
    @Inline
    public <T extends RRLibSerializable> void deepCopyFrom(@Const @Ptr T source, @CppDefault("NULL") @Ptr Factory f) {

        //JavaOnlyBlock
        assert(source.getClass() == type.getJavaClass());
        deepCopyFrom((Object)source, f);
        
        //Cpp assert(DataType<T>() == type);
        //Cpp deepCopyFrom((void*)source);
    }
    
    /**
     * Deep copy source object to this object
     * (types MUST match)
     * 
     * @param source Source object
     */
    protected abstract void deepCopyFrom(@Const @VoidPtr Object source, @CppDefault("NULL") @Ptr Factory f);
}
