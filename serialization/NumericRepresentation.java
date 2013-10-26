//
// You received this file as part of RRLib
// Robotics Research Library
//
// Copyright (C) Finroc GbR (finroc.org)
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
//
//----------------------------------------------------------------------
package org.rrlib.finroc_core_utils.serialization;

import org.rrlib.finroc_core_utils.rtti.DataType;


/**
 * @author Max Reichardt
 *
 * Abstract interface for all types that have a numeric representation
 */
public interface NumericRepresentation extends RRLibSerializable {

    public final static DataType<NumericRepresentation> TYPE = new DataType<NumericRepresentation>(NumericRepresentation.class);

    /**
     * @return number of blittable objects in this objects (Image lists may have multiple)
     */
    public Number getNumericRepresentation();

    /**
     * Empty Numeric
     */
    public class Empty extends RRLibSerializableImpl implements NumericRepresentation {

        public final static DataType<Empty> TYPE = new DataType<Empty>(Empty.class, "DummyNumeric");

        @Override
        public void serialize(OutputStreamBuffer os) {
        }

        @Override
        public void deserialize(InputStreamBuffer is) {
        }

        @Override
        public Number getNumericRepresentation() {
            return 0;
        }
    }
}
