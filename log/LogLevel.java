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
package org.rrlib.finroc_core_utils.log;

import org.rrlib.finroc_core_utils.jc.annotation.JavaOnly;

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
    LL_USER,             //!< Information for user (including end-users). Is always shown if domain is active.
    LL_ERROR,            //!< Error message. Used to inform about _certain_ malfunction of application. Is always shown if domain is active.
    LL_WARNING,          //!< Critical warning. Warns about possible application malfunction and invalid (and therefore discarded) user input. (default max level with _RRLIB_LOG_LESS_OUTPUT_)
    LL_DEBUG_WARNING,    //!< Debug info with warning character (e.g. "Parameter x not set - using default y")
    LL_DEBUG,            //!< Debug info about coarse program flow (default max level without _RRLIB_LOG_LESS_OUTPUT_) - information possibly relevant to developers outside of respective domain
    LL_DEBUG_VERBOSE_1,  //!< Higher detail debug info (not available in release mode) - only relevant to developers in respective domain
    LL_DEBUG_VERBOSE_2,  //!< Higher detail debug info (not available in release mode) - only relevant to developers in respective domain
    LL_DEBUG_VERBOSE_3,  //!< Higher detail debug info (not available in release mode) - only relevant to developers in respective domain
    LL_DIMENSION         //!< Endmarker and dimension of eLogLevel
};