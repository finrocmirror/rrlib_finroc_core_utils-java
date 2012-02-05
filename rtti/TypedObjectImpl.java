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
package org.rrlib.finroc_core_utils.rtti;

import org.rrlib.finroc_core_utils.jc.annotation.Const;
import org.rrlib.finroc_core_utils.jc.annotation.ConstMethod;
import org.rrlib.finroc_core_utils.jc.annotation.CppFilename;
import org.rrlib.finroc_core_utils.jc.annotation.CppName;
import org.rrlib.finroc_core_utils.jc.annotation.CppType;
import org.rrlib.finroc_core_utils.jc.annotation.InCpp;
import org.rrlib.finroc_core_utils.jc.annotation.Init;
import org.rrlib.finroc_core_utils.jc.annotation.Inline;
import org.rrlib.finroc_core_utils.jc.annotation.NoCpp;
import org.rrlib.finroc_core_utils.jc.annotation.NonVirtual;
import org.rrlib.finroc_core_utils.jc.annotation.Ptr;
import org.rrlib.finroc_core_utils.jc.annotation.Ref;
import org.rrlib.finroc_core_utils.jc.annotation.Superclass;
import org.rrlib.finroc_core_utils.jc.log.LogDefinitions;
import org.rrlib.finroc_core_utils.log.LogDomain;
import org.rrlib.finroc_core_utils.serialization.RRLibSerializableImpl;

/**
 * @author max
 *
 * This is the abstract base class for any object that has additional
 * type information as provided in this package.
 *
 * Such classes can be cleanly serialized to the network
 *
 * C++ issue: Typed objects are not automatically jc objects!
 */
@Ptr @Inline @NoCpp
@CppName("TypedObject") @CppFilename("TypedObject")
@Superclass( {RRLibSerializableImpl.class})
public abstract class TypedObjectImpl extends RRLibSerializableImpl implements TypedObject {

    /** Type of object */
    protected DataTypeBase type;

    /** Log domain for serialization */
    @InCpp("_RRLIB_LOG_CREATE_NAMED_DOMAIN(logDomain, \"serialization\");")
    public static final LogDomain logDomain = LogDefinitions.finroc.getSubDomain("serialization");

    /**
     * @return Type of object
     */
    @Init("type(NULL)")
    public TypedObjectImpl() {}

    @NonVirtual @ConstMethod public DataTypeBase getType() {
        return type;
    }

    /**
     * @return Log description (default implementation is "<class name> (<pointer>)"
     */
    @InCpp("return *this;") @NonVirtual
    public @ConstMethod @Const @Ref @CppType("TypedObject") String getLogDescription() {
        return getClass().getSimpleName() + " (@" + Integer.toHexString(hashCode()) + ")";
    }
}
