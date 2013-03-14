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
 * Management information for generic object.
 * May be subclassed to store more custom info such as reference counting.
 *
 * GenericObjectManagers are always written into memory allocated
 * by GenericObject subclass.
 * Therefore, their destructor should never be called. Instead, the
 * GenericObject should be deallocated.
 */
public interface GenericObjectManager {

    /**
     * @return Generic object that this class manages
     */
    public GenericObject getObject();

    /**
     * @param managedObject Set object to be managed
     */
    public void setObject(GenericObject managedObject);
}

