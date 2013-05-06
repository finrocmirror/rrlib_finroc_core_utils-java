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
package org.rrlib.finroc_core_utils.jc;

/**
 * @author Max Reichardt
 *
 * Stores order for locking objects.
 * Contains two integer numbers - primary and secondary - and
 * can be compared to other MutexLockOrder objects.
 *
 * Object should be locked from small to high lock numbers.
 * In C++ this order is enforced, if #defines are set accordingly.
 */
public class MutexLockOrder {

    /** Primary is evaluated first, if equal, secondary is compared */
    private final int primary, secondary;

    public MutexLockOrder(int primary, int secondary) {
        this.primary = primary;
        this.secondary = secondary;
    }

    public MutexLockOrder(int primary) {
        this.primary = primary;
        this.secondary = 0;
    }

    /**
     * @param other Other Lock order
     * @return Is it valid to lock this object after object with the other mutex lock order?
     */
    public boolean validAfter(MutexLockOrder other) {
        if (primary < other.primary) {
            return false;
        } else if (primary > other.primary) {
            return true;
        } else {
            if (secondary == other.secondary) {
                throw new RuntimeException("Equal lock orders are not allowed");
            }
            return secondary > other.secondary;
        }
    }

    /**
     * @return Primary lock order (higher is later)
     */
    public int getPrimary() {
        return primary;
    }

    /**
     * @return Secondary lock order (higher is later)
     */
    public int getSecondary() {
        return secondary;
    }
}
