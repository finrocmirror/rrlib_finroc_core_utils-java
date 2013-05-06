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
package org.rrlib.finroc_core_utils.rtti;

import org.rrlib.finroc_core_utils.jc.log.LogDefinitions;
import org.rrlib.finroc_core_utils.log.LogDomain;
import org.rrlib.finroc_core_utils.serialization.RRLibSerializableImpl;

/**
 * @author Max Reichardt
 *
 * This is the abstract base class for any object that has additional
 * type information as provided in this package.
 *
 * Such classes can be cleanly serialized to the network
 *
 * C++ issue: Typed objects are not automatically jc objects!
 */
public abstract class TypedObjectImpl extends RRLibSerializableImpl implements TypedObject {

    /** Type of object */
    protected DataTypeBase type;

    /** Log domain for serialization */
    public static final LogDomain logDomain = LogDefinitions.finroc.getSubDomain("serialization");

    /**
     * @return Type of object
     */
    public TypedObjectImpl() {}

    public DataTypeBase getType() {
        return type;
    }

    /**
     * @return Log description (default implementation is "<class name> (<pointer>)"
     */
    public String getLogDescription() {
        return getClass().getSimpleName() + " (@" + Integer.toHexString(hashCode()) + ")";
    }
}
