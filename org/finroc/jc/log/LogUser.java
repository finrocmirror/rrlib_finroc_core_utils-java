/**
 * You received this file as part of an advanced experimental
 * robotics framework prototype ('finroc')
 *
 * Copyright (C) 2010 Max Reichardt,
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
package org.finroc.jc.log;

import org.finroc.jc.annotation.Const;
import org.finroc.jc.annotation.ConstMethod;
import org.finroc.jc.annotation.CppType;
import org.finroc.jc.annotation.HAppend;
import org.finroc.jc.annotation.HPrepend;
import org.finroc.jc.annotation.InCpp;
import org.finroc.jc.annotation.Inline;
import org.finroc.jc.annotation.JavaOnly;
import org.finroc.jc.annotation.NoCpp;
import org.finroc.jc.annotation.NonVirtual;
import org.finroc.jc.annotation.PostProcess;
import org.finroc.jc.annotation.Ref;
import org.finroc.log.LogDomain;
import org.finroc.log.LogLevel;

/**
 * @author max
 *
 * Abstract log user.
 * Having this class as base class makes logging more convenient.
 *
 * Furthermore, objects can have (debug) logging turned on and off for every instance separately
 */
@HPrepend("typedef rrlib::logging::eLogLevel eLogLevel;")
@HAppend( {"inline std::ostream& operator << (std::ostream& output, const LogUser* lu) {",
           "    output << typeid(*lu).name() << \" (\" << ((void*)lu) << \")\";",
           "    return output;",
           "}",
           "inline std::ostream& operator << (std::ostream& output, const LogUser& lu) {",
           "    output << (&lu);",
           "    return output;",
           "}"
          })
@Inline @NoCpp
public class LogUser {

    /**
     * @return Log description (default implementation is "<class name> (<pointer>)"
     */
    @InCpp("return *this;")
    public @ConstMethod @Const @Ref @CppType("LogUser") String getLogDescription() {
        return super.toString();
    }

    /**
     * @return Show Debug messages for this specific instance
     */
    @NonVirtual @ConstMethod
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
    @JavaOnly @PostProcess("org.finroc.j2c.LogUserMessage") @ConstMethod
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
    @JavaOnly @PostProcess("org.finroc.j2c.LogUserMessage") @ConstMethod
    public void objectLog(LogLevel level, LogDomain domain, String msg) {
        if (level.ordinal() <= LogLevel.LL_WARNING.ordinal() || showObjectDebugMessages()) {
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
    @JavaOnly @PostProcess("org.finroc.j2c.LogUserMessage") @ConstMethod
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
    @JavaOnly @PostProcess("org.finroc.j2c.LogUserMessage") @ConstMethod
    public void objectLog(LogLevel level, LogDomain domain, Exception e) {
        if (level.ordinal() <= LogLevel.LL_WARNING.ordinal() || showObjectDebugMessages()) {
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
    @JavaOnly @PostProcess("org.finroc.j2c.LogUserMessage") @ConstMethod
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
    @JavaOnly @PostProcess("org.finroc.j2c.LogUserMessage") @ConstMethod
    public void objectLog(LogLevel level, LogDomain domain, String msg, Exception e) {
        if (level.ordinal() <= LogLevel.LL_WARNING.ordinal() || showObjectDebugMessages()) {
            domain.log(level, getLogDescription(), msg, e, 2);
        }
    }

}
