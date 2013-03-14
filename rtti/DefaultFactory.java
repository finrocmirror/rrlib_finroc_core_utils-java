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
 * Default factory implementation.
 * Simply allocates and deletes objects as needed on Heap.
 */
public class DefaultFactory implements Factory {

    public static DefaultFactory instance;

    @Override
    public Object createBuffer(DataTypeBase dt) {
        return dt.createInstance();
    }

    @Override
    public GenericObject createGenericObject(DataTypeBase dt, Object customParameter) {
        return dt.createInstanceGeneric(null);
    }
}