/**
 * You received this file as part of an advanced experimental
 * robotics framework prototype ('finroc')
 *
 * Copyright (C) 2010 Robotics Research Lab, University of Kaiserslautern
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
package org.finroc.log;

import org.finroc.jc.annotation.JavaOnly;

/**
* The enumeration that encodes the message levels. These levels are
* predefined and can be used to give the messages different priorities,
* as messages with too low level will be suppressed by a configuration
* setting or when _RRLIB_LOGGING_LESS_OUTPUT_ is defined (e.g. in
* release mode).
* They are also used for colored output to stdout or stderr.
 */
@JavaOnly
public enum LogLevel {
    eLL_VERBOSE,    //!< Messages of this level should only be used for debugging purposes
    eLL_LOW,        //!< Lower level message (not processed when _RRLIB_LOGGING_LESS_OUTPUT_ is defined)
    eLL_MEDIUM,     //!< Medium level messages (default min. level when _RRLIB_LOGGING_LESS_OUTPUT_ is not defined)
    eLL_HIGH,       //!< Higher level messages (default min. level when _RRLIB_LOGGING_LESS_OUTPUT_ is defined)
    eLL_ALWAYS,     //!< Messages of this level are always shown if the domain is active
    eLL_DIMENSION   //!< Endmarker and dimension of eLogLevel
};