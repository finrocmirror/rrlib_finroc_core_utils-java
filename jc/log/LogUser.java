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
package org.rrlib.finroc_core_utils.jc.log;

import org.rrlib.finroc_core_utils.log.LogDomain;
import org.rrlib.finroc_core_utils.log.LogLevel;

/**
 * @author Max Reichardt
 *
 * Abstract log user.
 * Having this class as base class makes logging more convenient.
 *
 * Furthermore, objects can have (debug) logging turned on and off for every instance separately
 */
public class LogUser {

    /**
     * @return Log description (default implementation is "<class name> (<pointer>)"
     */
    public String getLogDescription() {
        return getClass().getSimpleName() + " (@" + Integer.toHexString(hashCode()) + ")";
    }

    /**
     * @return Show Debug messages for this specific instance
     */
    public boolean showObjectDebugMessages() {
        return true;
    }

    /**
     * Log message.
     *
     * @param level Log level
     * @param domain Logging domain
     * @param msg Log message
     */
    public void log(LogLevel level, LogDomain domain, String msg) {
        domain.log(level, getLogDescription(), msg, null, 2);
    }

    /**
     * Log message.
     * Debug messages will only be output if showDebugMessages() returns true.
     *
     * @param level Log level
     * @param domain Logging domain
     * @param msg Log message
     */
    public void objectLog(LogLevel level, LogDomain domain, String msg) {
        if (level.ordinal() <= LogLevel.WARNING.ordinal() || showObjectDebugMessages()) {
            domain.log(level, getLogDescription(), msg, null, 2);
        }
    }

    /**
     * Log exception.
     *
     * @param level Log level
     * @param domain Logging domain
     * @param e Exception
     */
    public void log(LogLevel level, LogDomain domain, Exception e) {
        domain.log(level, getLogDescription(), "", e, 2);
    }

    /**
     * Log exception.
     * Debug messages will only be output if showDebugMessages() returns true.
     *
     * @param level Log level
     * @param domain Logging domain
     * @param e Exception
     */
    public void objectLog(LogLevel level, LogDomain domain, Exception e) {
        if (level.ordinal() <= LogLevel.WARNING.ordinal() || showObjectDebugMessages()) {
            domain.log(level, getLogDescription(), "", e, 2);
        }
    }

    /**
     * Log message and exception.
     *
     * @param level Log level
     * @param domain Logging domain
     * @param msg Log message
     * @param e Exception
     */
    public void log(LogLevel level, LogDomain domain, String msg, Exception e) {
        domain.log(level, getLogDescription(), msg, e, 2);
    }

    /**
     * Log message and exception.
     * Debug messages will only be output if showDebugMessages() returns true.
     *
     * @param level Log level
     * @param domain Logging domain
     * @param msg Log message
     * @param e Exception
     */
    public void objectLog(LogLevel level, LogDomain domain, String msg, Exception e) {
        if (level.ordinal() <= LogLevel.WARNING.ordinal() || showObjectDebugMessages()) {
            domain.log(level, getLogDescription(), msg, e, 2);
        }
    }

}
