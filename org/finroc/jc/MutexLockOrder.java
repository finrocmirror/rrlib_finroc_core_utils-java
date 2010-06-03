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
package org.finroc.jc;

import org.finroc.jc.annotation.Const;
import org.finroc.jc.annotation.ConstMethod;
import org.finroc.jc.annotation.JavaOnly;
import org.finroc.jc.annotation.Ref;

/**
 * @author max
 *
 * Stores order for locking objects.
 * Contains two integer numbers - primary and secondary - and
 * can be compared to other MutexLockOrder objects.
 *
 * Object should be locked from small to high lock numbers.
 * In C++ this order is enforced, if #defines are set accordingly.
 */
@JavaOnly
public class MutexLockOrder {

    /** Primary is evaluated first, if equal, secondary is compared */
    private final int primary, secondary;

    public MutexLockOrder(int primary, int secondary) {
        this.primary = primary;
        this.secondary = secondary;
    }

    @JavaOnly
    public MutexLockOrder(int primary) {
        this.primary = primary;
        this.secondary = 0;
    }

    /**
     * @param other Other Lock order
     * @return Is it valid to lock this object after object with the other mutex lock order?
     */
    @ConstMethod public boolean validAfter(@Const @Ref MutexLockOrder other) {
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
