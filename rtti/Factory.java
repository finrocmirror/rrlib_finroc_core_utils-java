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

/**
 * @author Max Reichardt
 *
 * Factory to create objects (as shared_ptr) needed in input streams or
 * during deep copy operations.
 *
 * When deserializing pointer list, for example, buffers are needed.
 *
 * It may be specialized for more efficient buffer management.
 */
public interface Factory {

    /**
     * Create buffer
     * (used to fill vectors)
     *
     * @param dt Data type
     * @return Created buffer
     */
    public Object createBuffer(DataTypeBase dt);

    /**
     * Create generic object
     * (used in writeObject() / readObject() of stream classes)
     *
     * @param dt Data type
     * @param factoryParameter Custom factory parameter
     * @return Created buffer
     */
    public GenericObject createGenericObject(DataTypeBase dt, Object factoryParameter);

}
